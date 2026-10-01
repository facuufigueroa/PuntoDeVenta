import Model.TablaVenta;
import View.VentaView;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.TableColumn;

public final class TablaPrecioCheck {
    private static void check(VentaView view) {
        JTable table = view.tablaProductos;
        if (table.getTableHeader().getReorderingAllowed())
            throw new AssertionError("Columns must keep their meaning for the controller");
        if (table.editCellAt(0, 1)) throw new AssertionError("Price must not enter edit mode");
        TableColumn price = table.getColumnModel().getColumn(1);
        price.setWidth(1);
        if (price.getWidth() < 140 || price.getResizable())
            throw new AssertionError("Price column can collapse");
        Component normal = table.prepareRenderer(table.getCellRenderer(0, 1), 0, 1);
        if (!normal.getFont().equals(table.getFont()))
            throw new AssertionError("Price font changed");
        if (!"$1234567890".equals(((JLabel) normal).getText()))
            throw new AssertionError("Price was truncated in the model/renderer");
        table.setRowSelectionInterval(0, 0);
        Component selected = table.prepareRenderer(table.getCellRenderer(0, 1), 0, 1);
        if (!selected.getFont().equals(table.getFont()))
            throw new AssertionError("Selected price font changed");
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            VentaView view = new VentaView();
            try {
                // Controller replaces the generated model at startup; test that exact transition.
                for (int pass = 0; pass < 2; pass++) {
                    TablaVenta model = new TablaVenta();
                    model.addRow(new Object[]{"Producto de prueba", "$1234567890"});
                    view.tablaProductos.setModel(model);
                    check(view);
                }
                System.out.println("OK: price width, read-only cells, stable font and model replacement");
            } finally { view.dispose(); }
        });
    }
}
