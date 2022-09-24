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
                iniciarJTable();
            }
            else{
                JOptionPane.showMessageDialog(null,"Hay campos vacios, rellene los campos para agregar el producto");
            }
        } else {
        }   
    }
    
    public boolean verificarVacios(){
        return adminView.txtCodigo.getText().isEmpty() 
                && adminView.txtNombre.getText().isEmpty()
                && adminView.txtPrecio.getText().isEmpty();
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
    
    
    public Querys getQuery() {
        return query;
    }

    public void setQuery(Querys query) {
        this.query = query;
    }

   
}
