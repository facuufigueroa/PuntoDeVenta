package Controller;

import DataBase.Querys;
import Model.Producto;
import View.AdministracionView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Integer.parseInt;
import javax.swing.JOptionPane;


public class AdministracionController implements ActionListener{
    
    private Querys query = new Querys();
    
    private AdministracionView adminView= new AdministracionView();

    public AdministracionController() {
        
        adminView.btnAgregar.addActionListener(this);
        adminView.btnEditar.addActionListener(this);
        adminView.btnEliminar.addActionListener(this);
        this.adminView.btnExportar.addActionListener(this);
        this.adminView.btnModificar.addActionListener(this);
        this.adminView.txtBuscar.addActionListener(this);
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
    
    
    
    
    public Querys getQuery() {
        return query;
    }

    public void setQuery(Querys query) {
        this.query = query;
    }

   
}
