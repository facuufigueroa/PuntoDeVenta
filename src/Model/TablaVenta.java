package Model;

import javax.swing.table.DefaultTableModel;

/** Prices are changed through product entry, never by editing display cells. */
public final class TablaVenta extends DefaultTableModel {
    public TablaVenta() {
        super(new String[]{"Producto", "Precio"}, 0);
    }

    @Override public boolean isCellEditable(int row, int column) {
        return false;
    }
}
