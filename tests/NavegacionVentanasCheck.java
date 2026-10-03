import View.NavegacionVentanas;
import javax.swing.*;
import java.awt.event.WindowEvent;

public final class NavegacionVentanasCheck {
    private static JFrame padre, hija, nieta;
    private static void check(boolean ok) { if (!ok) throw new AssertionError("Navegación incorrecta"); }
    public static void main(String[] args) throws Exception {
        try {
            SwingUtilities.invokeAndWait(() -> {
                padre=new JFrame();hija=new JFrame();nieta=new JFrame();
                padre.setSize(300,200);hija.setSize(300,200);nieta.setSize(300,200);
                padre.setVisible(true);
                NavegacionVentanas.abrir(padre,hija);
                check(!padre.isVisible() && hija.isVisible());
                NavegacionVentanas.abrir(hija,nieta);
                check(!padre.isVisible() && !hija.isVisible() && nieta.isVisible());
                nieta.dispatchEvent(new WindowEvent(nieta,WindowEvent.WINDOW_CLOSING));
            });
            SwingUtilities.invokeAndWait(() -> {check(hija.isVisible() && !padre.isVisible());hija.dispose();});
            SwingUtilities.invokeAndWait(() -> {
                check(padre.isVisible());
                int listeners=hija.getWindowListeners().length;
                NavegacionVentanas.abrir(padre,hija);
                check(hija.getWindowListeners().length==listeners && !padre.isVisible());
                hija.dispose();
            });
            SwingUtilities.invokeAndWait(() -> {
                check(padre.isVisible());NavegacionVentanas.abrir(padre,hija,false);
                check(padre.isVisible() && hija.isVisible());
                hija.dispose();
            });
            SwingUtilities.invokeAndWait(() -> {
                check(padre.isVisible());NavegacionVentanas.abrir(padre,hija);
                padre.dispose();hija.dispose();
            });
            SwingUtilities.invokeAndWait(() -> check(!padre.isVisible()));
            System.out.println("OK: nested navigation, close button, reopening, no duplicate listeners or resurrection");
        } finally {
            SwingUtilities.invokeAndWait(() -> {if(nieta!=null)nieta.dispose();if(hija!=null)hija.dispose();if(padre!=null)padre.dispose();});
        }
    }
}
