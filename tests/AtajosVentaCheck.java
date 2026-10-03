import View.VentaView;
import java.awt.event.ActionEvent;
import javax.swing.*;

/** Standalone check: no database or printer required. Run on the Swing thread. */
public final class AtajosVentaCheck {
    private static void press(VentaView view, String key) {
        Object name = view.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(key));
        if (name == null) throw new AssertionError("Missing shortcut: " + key);
        view.getRootPane().getActionMap().get(name)
                .actionPerformed(new ActionEvent(view, ActionEvent.ACTION_PERFORMED, key));
    }

    private static void checkButton(VentaView view, String key, JButton button) {
        int[] clicks = {0};
        button.addActionListener(event -> {
            if (event.getSource() != button) throw new AssertionError("Wrong action source");
            clicks[0]++;
        });
        press(view, key);
        if (clicks[0] != 1) throw new AssertionError("Duplicate or missing action: " + key);
        button.setEnabled(false);
        press(view, key);
        if (clicks[0] != 1) throw new AssertionError("Disabled button activated: " + key);
        button.setEnabled(true);
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            VentaView view = new VentaView();
            try {
                checkButton(view, "F2", view.btnNuevaCompra);
                checkButton(view, "F7", view.btnQuitarProducto);
                checkButton(view, "F8", view.btnObtenerVuelto);
                checkButton(view, "F9", view.btnImprimir);
                checkButton(view, "F10", view.btnCobrar);
                int[] priceCalls = {0};
                view.btnVerPrecio.getActionMap().put("pressedF4", new AbstractAction() {
                    @Override public void actionPerformed(ActionEvent event) { priceCalls[0]++; }
                });
                press(view, "F4");
                if (priceCalls[0] != 1) throw new AssertionError("F4 must open price once");
                view.txtPagaCon.setText("10000");
                press(view, "F3");
                if (!"10000".equals(view.txtPagaCon.getSelectedText()))
                    throw new AssertionError("F3 must select the amount");
                view.txtCodigo.setText("779123");
                press(view, "F5");
                if (!"779123".equals(view.txtCodigo.getSelectedText()))
                    throw new AssertionError("F5 must select the code");
                press(view, "F6");
                int[] additions = {0};
                view.btnAgregarOtro.addActionListener(event -> additions[0]++);
                view.txtPrecio.postActionEvent();
                if (additions[0] != 1) throw new AssertionError("Enter must add once");
                if (view.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .get(KeyStroke.getKeyStroke("F1")) == null)
                    throw new AssertionError("Missing keyboard help");
                System.out.println("OK: shortcuts, shared actions, disabled buttons and Enter");
            } finally { view.dispose(); }
        });
    }
}
