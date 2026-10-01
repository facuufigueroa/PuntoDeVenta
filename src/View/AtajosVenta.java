package View;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/** Window-scoped shortcuts share the same actions and confirmations as clicks. */
public final class AtajosVenta {
    private AtajosVenta() {}

    public static void instalar(VentaView view) {
        JRootPane root = view.getRootPane();
        boton(root, "F2", view.btnNuevaCompra);
        accion(root, "F3", () -> enfocar(view.txtPagaCon));
        boton(root, "F4", view.btnVerPrecio);
        accion(root, "F5", () -> enfocar(view.txtCodigo));
        accion(root, "F6", () -> {
            view.cbbNombre.scrollRectToVisible(new java.awt.Rectangle(0, 0,
                    view.cbbNombre.getWidth(), view.cbbNombre.getHeight()));
            view.cbbNombre.requestFocusInWindow();
        });
        boton(root, "F7", view.btnQuitarProducto);
        boton(root, "F8", view.btnObtenerVuelto);
        boton(root, "F9", view.btnImprimir);
        accion(root, "F1", () -> JOptionPane.showMessageDialog(view,
                "F2  ·  Nueva compra (pide confirmación)\n"
                + "F3  ·  Ir a Paga con\n"
                + "F4  ·  Consultar precio\n"
                + "F5  ·  Volver al código de barras\n"
                + "F6  ·  Ir a producto sin código\n"
                + "F7  ·  Quitar el producto seleccionado\n"
                + "F8  ·  Calcular vuelto\n"
                + "F9  ·  Imprimir ticket\n\n"
                + "Enter en código: agregar producto\n"
                + "Enter en Paga con: calcular vuelto\n"
                + "Enter en precio sin código: agregar al carrito\n"
                + "Tab / Shift+Tab: avanzar / retroceder entre controles\n"
                + "Esc en consulta de precio: cerrar la consulta",
                "Atajos de teclado", JOptionPane.INFORMATION_MESSAGE));
        view.txtPrecio.addActionListener(event -> view.btnAgregarOtro.doClick(0));
        view.btnNuevaCompra.setText("Nueva compra · F2");
        view.btnVerPrecio.setText("Ver precio · F4");
        view.btnQuitarProducto.setText("Quitar · F7");
        view.btnObtenerVuelto.setText("Calcular vuelto · F8");
        view.btnImprimir.setText("Imprimir ticket · F9");
        view.txtPagaCon.setToolTipText("F3: ingresar pago. Enter: calcular vuelto.");
        view.txtCodigo.setToolTipText("F5: volver al lector. Enter: agregar producto.");
        view.cbbNombre.setToolTipText("F6: agregar un producto sin código.");
        view.txtPrecio.setToolTipText("Enter: agregar el producto sin código al carrito.");
    }

    private static void enfocar(javax.swing.JTextField field) {
        field.scrollRectToVisible(new java.awt.Rectangle(0, 0, field.getWidth(), field.getHeight()));
        field.requestFocusInWindow();
        field.selectAll();
    }

    private static void boton(JRootPane root, String key, JButton button) {
        accion(root, key, () -> {
            if (button.isEnabled()) button.doClick(0);
        });
    }

    private static void accion(JRootPane root, String key, Runnable action) {
        String name = "venta." + key;
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        root.getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }
}
