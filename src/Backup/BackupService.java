package Backup;

import DataBase.ConexionBD;
import java.io.*;
import java.net.URI;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.*;

/** Weekly backups and catch-up run outside Swing's event thread. */
public final class BackupService implements AutoCloseable {
    private final Path settingsFile;
    private final Properties settings = new Properties();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "BruYen-backup");
        thread.setDaemon(true);
        return thread;
    });
    private volatile String status = "Esperando la comprobación semanal.";
    private volatile boolean running;
    interface DumpOperation { Path run(Path directory, String executable) throws Exception; }
    private final DumpOperation dumper;

    public BackupService(Path root) throws IOException {
        this(root, BackupService::dump);
    }

    BackupService(Path root, DumpOperation dumper) throws IOException {
        this.dumper = dumper;
        root = root.toAbsolutePath().normalize();
        settingsFile = root.resolve("config/backup.properties");
        if (Files.exists(settingsFile)) try (InputStream stream = Files.newInputStream(settingsFile)) {
            settings.load(stream);
        }
        if (!settings.containsKey("directory")) settings.setProperty("directory", root.toAbsolutePath().resolve("backups").toString());
        if (!settings.containsKey("enabled")) settings.setProperty("enabled", "true");
        if (!settings.containsKey("executable")) settings.setProperty("executable", detectExecutable());
        if (!get("lastSuccess").isEmpty()) status = "Última copia completada correctamente.";
    }

    public synchronized String get(String name) { return settings.getProperty(name, ""); }
    public String getStatus() { return status; }
    public boolean isRunning() { return running; }

    public synchronized void configure(String directory, String executable, boolean enabled) throws IOException {
        if (directory.trim().isEmpty() || executable.trim().isEmpty()) throw new IOException("Elegí una carpeta y el programa mysqldump.");
        Properties previous = new Properties();
        previous.putAll(settings);
        settings.setProperty("directory", Paths.get(directory).toAbsolutePath().normalize().toString());
        settings.setProperty("executable", executable.trim());
        settings.setProperty("enabled", Boolean.toString(enabled));
        try { save(); }
        catch (IOException error) { settings.clear(); settings.putAll(previous); throw error; }
    }

    private synchronized void save() throws IOException {
        Files.createDirectories(settingsFile.getParent());
        Path temporary = Files.createTempFile(settingsFile.getParent(), "backup-settings-", ".tmp");
        try {
            try (OutputStream stream = Files.newOutputStream(temporary)) { settings.store(stream, "Bru-Yen backup settings (no passwords)"); }
            move(temporary, settingsFile, true);
        } finally { Files.deleteIfExists(temporary); }
    }

    public void start() {
        worker.scheduleWithFixedDelay(() -> {
            try { execute(true); }
            catch (IOException error) { status = safeMessage(error); }
        }, 0, 30, TimeUnit.MINUTES);
    }
    public Future<Path> manual() { return worker.submit(() -> execute(false)); }

    public static LocalDate dueMonday(LocalDate today) {
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static boolean isDue(LocalDate today, String lastWeek) {
        try { return lastWeek.isEmpty() || LocalDate.parse(lastWeek).isBefore(dueMonday(today)); }
        catch (java.time.format.DateTimeParseException error) { return true; }
    }

    Path execute(boolean automatic) throws IOException {
        if (automatic && !Boolean.parseBoolean(get("enabled"))) {
            status = "Backup automático desactivado.";
            return null;
        }
        if (automatic && !isDue(LocalDate.now(), get("lastWeek"))) {
            status = "Al día. Próxima copia: lunes " + dueMonday(LocalDate.now()).plusWeeks(1);
            return null;
        }
        running = true;
        status = "Creando copia de seguridad…";
        try {
            Files.createDirectories(settingsFile.getParent());
            try (FileChannel channel = FileChannel.open(settingsFile.resolveSibling("backup.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock lock = channel.tryLock()) {
                if (lock == null) throw new IOException("Otra instancia del sistema está realizando un backup.");
                // Read the persisted week after taking the process lock to avoid duplicate weekly copies.
                if (automatic && Files.exists(settingsFile)) {
                    Properties persisted = new Properties();
                    try (InputStream stream = Files.newInputStream(settingsFile)) { persisted.load(stream); }
                    if (!isDue(LocalDate.now(), persisted.getProperty("lastWeek", ""))) {
                        synchronized (this) { settings.putAll(persisted); }
                        status = "La copia semanal ya fue realizada.";
                        return null;
                    }
                }
                LocalDate week = dueMonday(LocalDate.now());
                Path output = dumper.run(Paths.get(get("directory")), get("executable"));
                synchronized (this) {
                    Properties previous = new Properties();
                    previous.putAll(settings);
                    settings.setProperty("lastWeek", week.toString());
                    settings.setProperty("lastSuccess", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    settings.setProperty("lastFile", output.toString());
                    try { save(); }
                    catch (IOException error) { settings.clear(); settings.putAll(previous); throw error; }
                }
                status = "Copia completada: " + output.getFileName();
                return output;
            }
        } catch (Exception error) {
            status = "No se pudo completar la copia: " + safeMessage(error);
            if (!automatic) throw new IOException(status, error);
            return null; // No successful week is recorded: retry in 30 minutes or on next startup.
        } finally { running = false; }
    }

    static Path dump(Path directory, String executable) throws Exception {
        Files.createDirectories(directory);
        URI databaseUrl = URI.create(ConexionBD.URL.substring("jdbc:".length()));
        String database = databaseUrl.getPath().substring(1);
        String filename = "bru-yen_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8) + ".sql";
        Path output = directory.toAbsolutePath().resolve(filename);
        Path partial = Files.createTempFile(directory.toAbsolutePath(), "backup-", ".part");
        Path credentials = Files.createTempFile("bru-yen-client-", ".cnf");
        Path log = Files.createTempFile("bru-yen-dump-", ".log");
        Process process = null;
        try {
            protect(credentials);
            String config = "[client]\nhost=" + quote(databaseUrl.getHost())
                    + "\nport=" + (databaseUrl.getPort() < 0 ? 3306 : databaseUrl.getPort())
                    + "\nuser=" + quote(ConexionBD.USERNAME) + "\npassword=" + quote(ConexionBD.PASSWORD) + "\n";
            Files.write(credentials, config.getBytes(StandardCharsets.UTF_8));
            ProcessBuilder command = new ProcessBuilder(executable,
                    "--defaults-extra-file=" + credentials, "--single-transaction", "--quick",
                    "--routines", "--events", "--triggers", "--hex-blob", "--no-tablespaces",
                    "--set-gtid-purged=OFF", "--default-character-set=utf8mb4", "--comments",
                    "--result-file=" + partial, "--databases", database);
            command.redirectErrorStream(true).redirectOutput(log.toFile());
            process = command.start();
            if (!process.waitFor(10, TimeUnit.MINUTES)) throw new IOException("MySQL tardó más de 10 minutos. Se reintentará más tarde.");
            if (process.exitValue() != 0) {
                String diagnostic;
                try (InputStream stream = Files.newInputStream(log)) {
                    byte[] bytes = new byte[4096];
                    int count = stream.read(bytes);
                    diagnostic = count <= 0 ? "sin detalle" : new String(bytes, 0, count, StandardCharsets.UTF_8);
                }
                throw new IOException("mysqldump devolvió un error: " + diagnostic);
            }
            if (Files.size(partial) < 100) throw new IOException("MySQL produjo una copia vacía.");
            try (RandomAccessFile file = new RandomAccessFile(partial.toFile(), "r")) {
                file.seek(Math.max(0, file.length() - 4096));
                byte[] tail = new byte[(int) (file.length() - file.getFilePointer())];
                file.readFully(tail);
                if (!new String(tail, StandardCharsets.UTF_8).contains("-- Dump completed"))
                    throw new IOException("La copia no tiene la marca de finalización de MySQL.");
            }
            move(partial, output, false);
            return output;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
            Files.deleteIfExists(credentials);
            Files.deleteIfExists(log);
            Files.deleteIfExists(partial);
        }
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    private static void protect(Path file) throws IOException {
        AclFileAttributeView acl = Files.getFileAttributeView(file, AclFileAttributeView.class);
        if (acl != null) {
            acl.setAcl(Collections.singletonList(AclEntry.newBuilder().setType(AclEntryType.ALLOW)
                    .setPrincipal(Files.getOwner(file)).setPermissions(EnumSet.allOf(AclEntryPermission.class)).build()));
        } else Files.setPosixFilePermissions(file, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
    }

    private static void move(Path from, Path to, boolean replace) throws IOException {
        try {
            if (replace) Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            else Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException error) {
            if (replace) Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            else Files.move(from, to);
        }
    }

    private static String detectExecutable() {
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles != null) {
            Path mysql = Paths.get(programFiles, "MySQL");
            if (Files.isDirectory(mysql)) try (DirectoryStream<Path> dirs = Files.newDirectoryStream(mysql, "MySQL Server*")) {
                for (Path dir : dirs) {
                    Path executable = dir.resolve("bin/mysqldump.exe");
                    if (Files.isRegularFile(executable)) return executable.toString();
                }
            } catch (IOException ignored) { }
        }
        return "mysqldump";
    }

    private static String safeMessage(Exception error) {
        String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        return ConexionBD.PASSWORD.isEmpty() ? message : message.replace(ConexionBD.PASSWORD, "[oculto]");
    }

    @Override public void close() { worker.shutdownNow(); }
}
