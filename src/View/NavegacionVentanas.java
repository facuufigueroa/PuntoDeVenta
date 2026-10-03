package View;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/** Keeps the current screen visible and restores its parent on disposal. */
public final class NavegacionVentanas {
    private static final String PADRE = "navegacion.padre", HIJA = "navegacion.hija";
    private NavegacionVentanas() {}

    public static void abrir(JFrame padre, JFrame hija) {
        abrir(padre, hija, !(padre instanceof VentaView));
    }

    public static void abrir(JFrame padre, JFrame hija, boolean ocultarPadre) {
        if (padre == null || padre == hija) { hija.setVisible(true); return; }
        if (hija.getRootPane().getClientProperty(PADRE) == null) {
            hija.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent event) {
                    if (hija.isDisplayable()) return;
                    JFrame anterior = (JFrame) hija.getRootPane().getClientProperty(PADRE);
                    if (anterior != null && anterior.getRootPane().getClientProperty(HIJA) == hija) {
                        anterior.getRootPane().putClientProperty(HIJA, null);
                        if (anterior.isDisplayable()) {
                            anterior.setVisible(true);
                            anterior.toFront();
                            anterior.requestFocus();
                        }
                    }
                }
            });
        }
        hija.getRootPane().putClientProperty(PADRE, padre);
        padre.getRootPane().putClientProperty(HIJA, hija);
        hija.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        hija.setLocationRelativeTo(padre);
        hija.setVisible(true);
        if (ocultarPadre) padre.setVisible(false);
    }
}
