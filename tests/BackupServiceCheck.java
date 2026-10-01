package Backup;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public final class BackupServiceCheck {
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }

    public static void main(String[] args) throws Exception {
        check(BackupService.isDue(LocalDate.of(2026, 10, 12), "2026-10-05"), "Monday is due");
        check(BackupService.isDue(LocalDate.of(2026, 10, 13), "2026-10-05"), "Closed Monday is caught up Tuesday");
        check(!BackupService.isDue(LocalDate.of(2026, 10, 18), "2026-10-12"), "No duplicates that week");
        check(BackupService.isDue(LocalDate.of(2027, 1, 5), "2026-12-28"), "Catch-up across year boundary");
        check(BackupService.isDue(LocalDate.now(), ""), "First installation creates a baseline");
        Path root = Files.createTempDirectory("bruyen-backup-test-");
        AtomicInteger calls = new AtomicInteger();
        try (BackupService service = new BackupService(root, (directory, executable) -> {
            if (calls.incrementAndGet() == 1) throw new IOException("Simulated unavailable database");
            Files.createDirectories(directory);
            return Files.write(directory.resolve("example.sql"), "-- Example successful backup".getBytes("UTF-8"));
        })) {
            service.execute(true);
            check(service.get("lastWeek").isEmpty(), "Failure must not mark week complete");
            Path copy = service.execute(true);
            check(copy != null && Files.exists(copy), "Pending backup retries successfully");
            service.execute(true);
            check(calls.get() == 2, "Completed week not backed up twice");
        }
        try (BackupService reloaded = new BackupService(root, (directory, executable) -> {
            throw new AssertionError("Persisted success must prevent duplicate");
        })) {
            check(!reloaded.get("lastSuccess").isEmpty(), "Success survives restart");
            reloaded.execute(true);
            reloaded.configure(root.resolve("backups").toString(), "mysqldump", false);
            check(reloaded.execute(true) == null, "Disabled schedule does not run");
        }
        System.out.println("OK: Monday, holiday catch-up, year change, failures, retries and persisted state");
        if (args.length > 0 && args[0].equals("--real-backup")) {
            try (BackupService real = new BackupService(Paths.get("."))) {
                Path file = real.manual().get();
                check(Files.size(file) > 100, "Real dump is not empty");
                System.out.println("REAL BACKUP OK: " + file + " (" + Files.size(file) + " bytes)");
            }
        }
    }
}
