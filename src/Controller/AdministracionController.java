package Controller;

import DataBase.Querys;
import Model.Producto;
import View.AdministracionView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Integer.parseInt;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


public class AdministracionController implements ActionListener{
    
    private Querys query = new Querys();
    
    private AdministracionView adminView= new AdministracionView();
    
    private DefaultTableModel modelo = new DefaultTableModel();
    
    public AdministracionController() {
        
        adminView.btnAgregar.addActionListener(this);
        adminView.btnEditar.addActionListener(this);
        adminView.btnEliminar.addActionListener(this);
        this.adminView.btnExportar.addActionListener(this);
        this.adminView.btnModificar.addActionListener(this);
        this.adminView.txtBuscar.addActionListener(this);
        iniciarJTable();
    }

   

    @Override
    public void actionPerformed(ActionEvent e) {
       agregarProducto(e);
       editarProducto(e);
       modificarProducto(e);
       borrarProducto(e);

    }

   
    
    public void loadAdminView(){
       adminView.setVisible(true);
       adminView.setLocationRelativeTo(null);
    }
    
    public void agregarProducto(ActionEvent e){
        Producto producto = new Producto();
        if(e.getSource() == adminView.btnAgregar){
            if(!verificarVacios()){
                producto.setCodigo(adminView.txtCodigo.getText());
                producto.setNombre(adminView.txtNombre.getText());
                producto.setPrecio(parseInt(adminView.txtPrecio.getText()));
                query.agregarProducto(producto);
                JOptionPane.showMessageDialog(null,"Producto: '"+adminView.txtNombre.getText()+"' registrado exitosamente");
                iniciarJTable();
                limpiarTxt();
            }
            else{
                JOptionPane.showMessageDialog(null,"Hay campos vacios, rellene los campos para agregar el producto");
            }
        } else {
        }   
    }
    
    public void iniciarJTable() {
        modelo = new DefaultTableModel(){
            public boolean isCellEditable(int fila, int columna){
                if(columna == 1 && columna == 2 && columna == 3){
                    return true;
                }
                else{
                    return false;
                }
            }
        };
        modelo.addColumn("CODIGO");
        modelo.addColumn("NOMBRE");
        modelo.addColumn("PRECIO");
        
        adminView.tablaProductos.setRowHeight(30);
        adminView.tablaProductos.setModel(modelo);
        query.listarProductos(modelo);
    }
    
    public void editarProducto(ActionEvent e) {
        if (e.getSource() == adminView.btnEditar) {
            int fila = adminView.tablaProductos.getSelectedRow();

            if (fila >= 0) {
                Producto producto = new Producto();
                producto.setCodigo(adminView.tablaProductos.getValueAt(fila, 0).toString());
                producto.setNombre(adminView.tablaProductos.getValueAt(fila, 1).toString());
                producto.setPrecio(parseInt(adminView.tablaProductos.getValueAt(fila, 2).toString()));
                adminView.txtCodigo.setText(producto.getCodigo());
                adminView.txtNombre.setText(producto.getNombre());
                adminView.txtPrecio.setText(String.valueOf(producto.getPrecio()));
                adminView.txtCodigo.setEditable(false);
            } else {
                JOptionPane.showMessageDialog(null, "Fila no seleccionada, seleccione fila de la tabla");
            }
        }
    }
    
    public void modificarProducto(ActionEvent e) {

        if (e.getSource() == adminView.btnModificar) {
           
            if (!verificarVacios()) {
                Producto producto = new Producto();
                producto.setCodigo(adminView.txtCodigo.getText());
                producto.setNombre(adminView.txtNombre.getText());
                producto.setPrecio(parseInt(adminView.txtPrecio.getText()));
                producto.setId(query.traer_id_producto(adminView.txtCodigo.getText()));
                if (query.editar(producto)) {
                    JOptionPane.showMessageDialog(null, "Producto modificado exitosamente", "Modifcar Producto", 3);
                    iniciarJTable();
                    limpiarTxt();
                    adminView.txtCodigo.setEditable(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Error al modificar producto");

                }
            } else {
                    JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">Para modificar productos los campos no deben estar vacios</p/</html>", "ERROR AL MODIFICAR", 0);
            }

        }
    }
    
    public boolean verificarVacios(){
        return adminView.txtCodigo.getText().isEmpty() 
                || adminView.txtNombre.getText().isEmpty()
                || adminView.txtPrecio.getText().isEmpty();
    }
    
    public void limpiarTxt(){
        adminView.txtCodigo.setText("");
        adminView.txtNombre.setText("");
        adminView.txtPrecio.setText("");
    }
    
    public void borrarProducto(ActionEvent e) {

        if (e.getSource() == adminView.btnEliminar) {
            Producto producto = new Producto();
            String botones[] = {"Aceptar", "Cancelar"};
            int fila = adminView.tablaProductos.getSelectedRow();

            if (fila >= 0) {
                int eleccion = JOptionPane.showOptionDialog(adminView, "¿Desea eliminar el prodcuto: " + adminView.tablaProductos.getValueAt(fila, 1).toString().toUpperCase() + " ?", "Eliminar Producto", 0, 0, null, botones, this);

                if (eleccion == JOptionPane.YES_OPTION) {
                    producto.setCodigo(adminView.tablaProductos.getValueAt(fila, 0).toString());

                    if (query.borrar(producto)) {
                        JOptionPane.showMessageDialog(null, adminView.tablaProductos.getValueAt(fila, 1).toString() + " " + "eliminado");
                        iniciarJTable();
                    }
                } else if (eleccion == JOptionPane.NO_OPTION) {
                    JOptionPane.showMessageDialog(null, "Se ha cancelado operación");
                    limpiarTxt();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, seleccione producto a eliminar en la tabla");
            }

        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public Querys getQuery() {
        return query;
    }

    public void setQuery(Querys query) {
        this.query = query;
    }

   
}
