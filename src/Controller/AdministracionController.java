package Controller;

import DataBase.ConexionBD;
import DataBase.Querys;
import Model.Producto;
import View.AdministracionView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import static java.lang.Integer.parseInt;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


public class AdministracionController implements ActionListener,KeyListener{
    
    private ConexionBD conexion = new ConexionBD();
    
    private Querys query = new Querys();
    
    private AdministracionView adminView= new AdministracionView();
    
    private DefaultTableModel modelo = new DefaultTableModel();
    
    /*Table model para busqueda filtrada*/
    private DefaultTableModel dtm_datos = new DefaultTableModel();
    
    
    public AdministracionController() {
        
        adminView.btnAgregar.addActionListener(this);
        adminView.btnEditar.addActionListener(this);
        adminView.btnEliminar.addActionListener(this);
        this.adminView.tablaProductos.addKeyListener(this);
        this.adminView.btnModificar.addActionListener(this);
        this.adminView.txtBuscarPorCodigo.addKeyListener(accionEnterBuscarPorCodigo());
        this.adminView.txtBuscarPorNombre.addKeyListener(this);
        iniciarJTable();
        
    }

   @Override
    public void keyReleased(KeyEvent e) {
        busquedaFiltrada(e);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
       agregarProducto(e);
       editarProducto(e);
       modificarProducto(e);
       borrarProducto(e);
       
    
    }
    
    
    
  
    public void loadAdminView(){
       vaciarTodosLosTxt();//Esta llamada es por si el usuario cierra la ventana pero no el programa
       iniciarJTable(); //Esta llamada es por si el usuario cierra la ventana pero no el programa
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
    
    public void iniciarJTableBusqueda(DefaultTableModel model,String[] dato ) {
        model = new DefaultTableModel(){
            public boolean isCellEditable(int fila, int columna){
                if(columna == 1 && columna == 2 && columna == 3){
                    return true;
                }
                else{
                    return false;
                }
            }
        };
        model.addColumn("CODIGO");
        model.addColumn("NOMBRE");
        model.addColumn("PRECIO");
        model.addRow(dato);
        adminView.tablaProductos.setRowHeight(30);
        adminView.tablaProductos.setModel(model);
        
        
    }
    
    public void editarProducto(ActionEvent e) {
        if (e.getSource() == adminView.btnEditar) {
            int fila = adminView.tablaProductos.getSelectedRow();

            if (fila >= 0) {
                Producto producto = new Producto();
                producto.setCodigo(adminView.tablaProductos.getValueAt(fila, 0).toString());
                producto.setNombre(adminView.tablaProductos.getValueAt(fila, 1).toString());
                String precioCon$ = adminView.tablaProductos.getValueAt(fila, 2).toString();
                int preciosin$ = parseInt(precioCon$.substring(1,precioCon$.length()));
                
                //producto.setPrecio(parseInt(adminView.tablaProductos.getValueAt(fila, 2).toString()));
                producto.setPrecio(preciosin$);
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

    public void busquedaFiltrada(KeyEvent k){
       
            Connection conn = conexion.getConnection();
            String[] Titulos= {"CODIGO","NOMBRE","PRECIO"};
            int valor = 0;
            int cont = 0;
            ResultSet rs;
            String aux = "" + adminView.txtBuscarPorNombre.getText();//aqui obtenemos cada letra que ingresemos en el textfield en tiempo real
                try {
                    Statement st_cont = conn.createStatement(); //hacemos lo mismo que con el metodo mostrar, buscamos el numero de filas dela tabla
                    rs = st_cont.executeQuery("SELECT COUNT(*) FROM producto WHERE nombre LIKE'" + adminView.txtBuscarPorNombre.getText() + "%' ORDER BY nombre");//solo que esta ves usamos like
                    if (rs.next()) {// like nos ayudara a buscar nombres que tengan similitudes con lo que estamos escribiendo en el texfield
                        valor = rs.getInt(1); //una vez que obtenimos el numero de filas continuamos a sacar  el valor que buscamos
                    }

                        String [][] M_datos = new String[valor][4];
                        rs = st_cont.executeQuery("SELECT * FROM producto WHERE nombre LIKE'" + adminView.txtBuscarPorNombre.getText() + "%' ORDER BY nombre"); //aqui es donde buscaremos a a la persona en especifico o las personas
                        while (rs.next()) {
                            M_datos[cont][0] = rs.getString("codigo");
                            M_datos[cont][1] = rs.getString("nombre");
                            M_datos[cont][2] = "$ "+rs.getString("precio");
                            cont = cont + 1;
                        }
                        dtm_datos = new DefaultTableModel(M_datos, Titulos) {
                            public boolean isCellEditable(int row, int column) {//este metodo es muy util si no quieren que editen su tabla, 
                    return false;  //si quieren modificar los campos al dar clic entonces borren este metodo
                }
                        };
                        adminView.tablaProductos.setModel(dtm_datos);

                } catch (Exception e) {
                }
                
        
    }
    
    public void busquedaPorCodigo(){
            DefaultTableModel modelo= new DefaultTableModel();
            String[] dato = new String[3];
            String codigo = adminView.txtBuscarPorCodigo.getText();

            if (query.buscarPorCodigo(codigo).getCodigo() != null) {
                
                Producto producto = query.buscarPorCodigo(codigo);

                dato[0] = producto.getCodigo();
                dato[1] = producto.getNombre();
                dato[2] = String.valueOf(producto.getPrecio());
                
                modelo.addRow(dato);
                iniciarJTableBusqueda(modelo,dato);
                adminView.txtBuscarPorCodigo.setText(null);

            } else {
                JOptionPane.showMessageDialog(null, "NO EXISTE PRODUCTO", "ERROR AL BUSCAR", 0);
            }
    }

    public void vaciarTodosLosTxt(){
        adminView.txtCodigo.setText("");
        adminView.txtNombre.setText("");
        adminView.txtPrecio.setText("");
        adminView.txtBuscarPorCodigo.setText("");
    }

    public KeyListener accionEnterBuscarPorCodigo (){
        
        KeyListener k = new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
              
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    busquedaPorCodigo();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
  
            };
            return k;
    }
    
    public KeyListener accionEnterBuscarPorNombre (){
        
        KeyListener k = new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
              
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    busquedaPorCodigo();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
  
            };
            return k;
    }
                
    
    
    
    @Override
    public void keyTyped(KeyEvent e){
    }

    @Override
    public void keyPressed(KeyEvent e){
    }
    
    
}
