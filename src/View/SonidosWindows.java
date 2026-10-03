package View;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Uses the current Windows sound scheme, without bundling system audio files. */
public final class SonidosWindows {
    private static final ThreadPoolExecutor AUDIO = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2), task -> {
                Thread thread = new Thread(task, "sonidos-windows");
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.DiscardOldestPolicy());
    private SonidosWindows() {}

    public static void producto() { reproducir("default"); }
    public static void confirmar() { reproducir("exclamation"); }
    public static void exito() { reproducir("asterisk"); }
    public static void error() { reproducir("hand"); }

    private static void reproducir(String evento) {
        if (!Boolean.parseBoolean(System.getProperty("pdv.sonidos", "true"))
                || GraphicsEnvironment.isHeadless()) return;
        AUDIO.execute(() -> {
            try {
                Object sonido = Toolkit.getDefaultToolkit().getDesktopProperty("win.sound." + evento);
                if (sonido instanceof Runnable) ((Runnable) sonido).run();
            } catch (RuntimeException ignored) {
                // Audio failure must never interrupt a sale or a saved operation.
            }
        });
    }
}
