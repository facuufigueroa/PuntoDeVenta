import View.ConfirmacionView;
import java.awt.event.ActionEvent;
import javax.swing.*;

public final class ConfirmacionCheck {
    private static void key(ConfirmacionView dialog, String key) {
        Object name = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(key));
        if (name == null) throw new AssertionError("Missing key: " + key);
        dialog.getRootPane().getActionMap().get(name)
                .actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, key));
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ConfirmacionView accept = new ConfirmacionView(null, "¿Nueva compra?", "Carrito actual.", "Aceptar");
            if (accept.getRootPane().getDefaultButton() == null)
                throw new AssertionError("Accept must be the default button");
            key(accept, "pressed ENTER");
            if (!accept.isAceptado()) throw new AssertionError("Enter must accept");
            ConfirmacionView cancel = new ConfirmacionView(null, "¿Nueva compra?", "Carrito actual.", "Aceptar");
            key(cancel, "ESCAPE");
            if (cancel.isAceptado()) throw new AssertionError("Escape must cancel");
            ConfirmacionView close = new ConfirmacionView(null, "¿Nueva compra?", "Carrito actual.", "Aceptar");
            close.dispose();
            if (close.isAceptado()) throw new AssertionError("Closing must cancel");
            System.out.println("OK: Enter accepts, Escape and closing cancel");
        });
    }
}
