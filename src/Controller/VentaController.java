package Controller;

import DataBase.Querys;
import Model.Compra;
import Model.Producto;
import Model.TablaVenta;
import Reporte.Reporte;
import View.VentaView;
import View.VerPrecio;
import View.ConfirmacionView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;


public final class VentaController implements ActionListener,KeyListener {
    
    private Querys query = new Querys();
    
    public final VentaView ventaView = new VentaView();
    
    DefaultTableModel modeloVenta = new TablaVenta();
    
    VerPrecio verPrecioView = new VerPrecio();
    

    public VentaController() {
        ventaView.txtCodigo.setFocusable(true);
        this.ventaView.txtPagaCon.addKeyListener(accionPagaCon());
        this.ventaView.btnObtenerVuelto.addActionListener(this);
        ventaView.btnNuevaCompra.addActionListener(this);
        this.ventaView.btnAgregarOtro.addActionListener(this);
        this.ventaView.txtCodigo.addKeyListener(accionAgregarATabla());
        this.ventaView.btnQuitarProducto.addActionListener(this);
        this.ventaView.txtBuscarCodigo.addKeyListener(accionEnterBuscarCodigo());
        this.ventaView.btnLimpiarBuscar.addActionListener(this);
        this.ventaView.btnImprimir.addActionListener(this);
   
        this.verPrecioView.txtCodigo.addKeyListener(accionVerPrecio());
        asignarF4BtnVerPrecio();
        asignarESCBtnVerCerrar();
        iniciarTabla();
        iniciarcomboBoxRapido();
    }
    
       
    public void iniciarTabla(){
        ventaView.tablaProductos.setRowHeight(35);
        ventaView.tablaProductos.setModel(modeloVenta);
       
    }
    
    public void listarEnTabla(String codigo){
        query.listarProducto(codigo, modeloVenta);
    }
    
    public void loadVentaView(){
      
       ventaView.setVisible(true);
       ventaView.setLocationRelativeTo(null);
       
    }
    
    
    public Querys getQuery() {
        return query;
    }

    public void setQuery(Querys query) {
        this.query = query;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        accionObtenerVuelto(e); 
        accionNuevaCompra(e);
        accionAgregarOtro(e);
        accionBorrarProductoSeleccionado(e);
        limpiarCamposDeBusqueda(e);
        imprimirTicket(e);
        
    }
    
    
     public final KeyListener accionAgregarATabla (){
       
        KeyListener k = new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    listarEnTabla(ventaView.txtCodigo.getText());
                    ventaView.txtCodigo.setText("");
                    ventaView.txtCodigo.setFocusable(true);
                    sumarTotal(modeloVenta);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
            };
            return k;
    }
    
    
    public void sumarTotal(DefaultTableModel modelo){
        int t = 0;
        int p1=0;
        String p=null;
       
        if(modelo.getRowCount()>=0 ){
            for(int i=0;i<modelo.getRowCount();i++){
                
                String precioCon$ = ventaView.tablaProductos.getValueAt(i, 1).toString();
                int preciosin$ = parseInt(precioCon$.substring(1,precioCon$.length()));
              
                t+=preciosin$;
              
                ventaView.txtTotalAPagar.setText("$"+String.valueOf(t));
            }
            
        }
    }
    
    public final KeyListener accionPagaCon(){
       
        KeyListener k = new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    pagaCon();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
            };
            return k;
    }
    
    
    public void pagaCon(){
        
        if(ventaView.txtTotalAPagar.getText().length()!=0){
            if(ventaView.txtPagaCon.getText().length()!=0){
                int pagaCon = Integer.parseInt(ventaView.txtPagaCon.getText());
                
                String precioCon$ = ventaView.txtTotalAPagar.getText();
                int preciosin$ = parseInt(precioCon$.substring(1,precioCon$.length()));
                if(pagaCon >= preciosin$){    
                    int vuelto=pagaCon-preciosin$;

                    ventaView.txtVuelto.setText("$"+String.valueOf(vuelto));
                }
                else{
                    JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">El valor ingresado es menor al total."
                            + "<br>Verifique nuevamente</p></html>");
                }
                }
            else{
                JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">El campo PAGA CON esta vacío</p></html>");
            }
            }else{
                JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">No se realizo ninguna compra</p></html>");
                ventaView.txtPagaCon.setText("");
            }
    }
    
    public void accionObtenerVuelto(ActionEvent e){
        if(e.getSource() == ventaView.btnObtenerVuelto){
            if(ventaView.txtTotalAPagar.getText().length()!=0){
                if(ventaView.txtPagaCon.getText().length()!=0){
                    pagaCon();
                }
                else{
                   JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">El campo PAGA CON esta vacío</p></html>"); 
                }
            }
        }
        
        
    }
    
    public void accionNuevaCompra(ActionEvent e){
        if(e.getSource() == ventaView.btnNuevaCompra){
            if (ConfirmacionView.confirmar(ventaView, "¿Nueva compra?",
                    "Se vaciará el carrito actual para comenzar otra compra. Revisá que ya hayas terminado con esta venta.",
                    "Aceptar")) {
                modeloVenta.setRowCount(0);
                vaciarTextFields();
                ventaView.txtCodigo.setText("");
                javax.swing.SwingUtilities.invokeLater(() -> ventaView.txtCodigo.requestFocusInWindow());
            }
           
        }
       
    }
    
    public void vaciarTextFields(){
        ventaView.txtTotalAPagar.setText("");
        ventaView.txtPagaCon.setText("");
        ventaView.txtVuelto.setText("");
        ventaView.cbbNombre.setSelectedItem("");
        ventaView.txtPrecio.setText("");
    }
    
    
    public void accionAgregarOtro(ActionEvent e){
     
        String otro [] = new String[2];
        if(e.getSource() == ventaView.btnAgregarOtro){
            if(ventaView.cbbNombre.getSelectedIndex() != 0){
                if(!"".equals(ventaView.txtPrecio.getText())){
                  
                    otro[0]=(String) ventaView.cbbNombre.getSelectedItem();
                    otro[1]= "$"+ventaView.txtPrecio.getText();     
                    query.listarOtro(modeloVenta, otro);
                    
                    sumarTotal(modeloVenta);
                    ventaView.cbbNombre.setSelectedItem("");
                    ventaView.txtPrecio.setText("");
                    ventaView.txtCodigo.requestFocus();
                }
                else{
                    JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">Debe poner precio para el producto sin código</p></html>"); 
                    
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">Debe poner nombre o seleccionar una opción en el listado</p></html>"); 
            }
        }
    }

    
    public void iniciarcomboBoxRapido(){
        String[] productosRapidos={"","Carnes","Pan","Fiambre","Frutas & Verduras","Chorizo","Chorizo Seco","Morcilla","Alimento"};
        for(String items : productosRapidos){
            ventaView.cbbNombre.addItem(items);
        }
    }
    
    public void borrarProductoSeleccionado(){
        int fila = 0;
        try{
            fila=ventaView.tablaProductos.getSelectedRow();
            if(fila==-1){
                JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">Seleccione producto en la tabla que desee quitar</p></html>");  
            }else{
                if(ConfirmacionView.confirmar(ventaView, "¿Quitar producto?",
                        "Se quitará del carrito el producto seleccionado. Los demás productos se conservarán.",
                        "Quitar producto")){
                    restarTotal(fila);
                    modeloVenta.removeRow(fila);
                    actualizarVuelto();
                }
               
            } 
        }catch(Exception e){
            
        }
    }
    
    public void accionBorrarProductoSeleccionado(ActionEvent e){
        if(e.getSource() == ventaView.btnQuitarProducto){
            borrarProductoSeleccionado();
        }
    }
    
    public void restarTotal(int precioProductoEliminado){
        
        if(precioProductoEliminado >=0 ){
            
          String precioCon$= ventaView.tablaProductos.getValueAt(precioProductoEliminado, 1).toString();
                   
          int preciosin$ = parseInt(precioCon$.substring(1,precioCon$.length()));
          
          int precioTotalsin$ = parseInt(ventaView.txtTotalAPagar.getText().substring(1,ventaView.txtTotalAPagar.getText().length()));
          
          ventaView.txtTotalAPagar.setText("$"+String.valueOf(precioTotalsin$-preciosin$));
          
       }
       else{
          JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">Fila no seleccionado</p></html>");
          
       }
    }
    
    public void actualizarVuelto(){
        if(ventaView.txtTotalAPagar.getText().length()!=0){
            int pagaCon = Integer.parseInt(ventaView.txtPagaCon.getText());
            int precioTotalsin$ = parseInt(ventaView.txtTotalAPagar.getText().substring(1,ventaView.txtTotalAPagar.getText().length()));

            int vuelto=pagaCon-precioTotalsin$;
         
            ventaView.txtVuelto.setText("$"+String.valueOf(vuelto));
        }else{
            JOptionPane.showMessageDialog(null, "<html><p style = \"font:14px\">No se realizo ninguna compra</p></html>");
            ventaView.txtPagaCon.setText(null);
        }
    }
        
    public void busquedaPorCodigo(){
            String codigo = ventaView.txtBuscarCodigo.getText();
            if (query.buscarPorCodigo(codigo).getCodigo() != null) {
                Producto producto = query.buscarPorCodigo(codigo);
                ventaView.txtNombreBuscado.setText(producto.getNombre());
                ventaView.txtPrecioBuscado.setText("$"+producto.getPrecio());
                ventaView.txtBuscarCodigo.setText(null);
            } else {
                JOptionPane.showMessageDialog(null,"<html><p style = \"font:15px\"> El producto no está registrado o el codigo esta mal escrito ¡Verifique el codigo! </p></html","PRODUCTO NO ENCONTRADO",0);

            }
    }
    
    
    public KeyListener accionEnterBuscarCodigo(){
        
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
    
    public void limpiarCamposDeBusqueda(ActionEvent e){
        if(e.getSource() == ventaView.btnLimpiarBuscar){
            ventaView.txtNombreBuscado.setText("");
            ventaView.txtPrecioBuscado.setText("");
            ventaView.txtCodigo.requestFocus();
        }
    }
    
    public void imprimirTicket(ActionEvent e) {
        if(e.getSource() == ventaView.btnImprimir){
            Reporte report = new Reporte();
            
            report.conexionReporte(ventaView.txtTotalAPagar.getText(),recorrerJTable());
        }
    }
    
    public ArrayList<Compra> recorrerJTable(){
        ArrayList<Compra> Resultados = new ArrayList();
        Compra tipo;
        
        Resultados.clear();
        
        for (int i = 0; i < ventaView.tablaProductos.getRowCount(); i++) {
            tipo= new Compra( String.valueOf(ventaView.tablaProductos.getValueAt(i, 0)),String.valueOf(ventaView.tablaProductos.getValueAt(i, 1)));
            Resultados.add(tipo);
        }
        return Resultados;
      
    }
    
    public String obtenerTotal(){
        return ventaView.txtTotalAPagar.getText();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void keyPressed(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void keyReleased(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    private void asignarF4BtnVerPrecio() {
        // Shared action for the price button; F4 is bound on the root pane.
        ActionMap actionMap = ventaView.btnVerPrecio.getActionMap();

        // F4 is registered once on the root pane by AtajosVenta.
        actionMap.put("pressedF4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                verPrecioView.setVisible(true);
                verPrecioView.setLocationRelativeTo(null);
                verPrecioView.txtCodigo.setText("");
                verPrecioView.labelNombre.setText("");
                verPrecioView.labelPrecio.setText("");
                verPrecioView.txtCodigo.requestFocus();
                
                
            }
        });

    }
   
    public final KeyListener accionVerPrecio(){
       
        KeyListener k = new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    Producto producto = obtenerPrecioNombreProducto();
                    verPrecioView.labelNombre.setText(producto != null && producto.getNombre() != null
                            ? producto.getNombre() : "Producto no encontrado");
                    verPrecioView.labelPrecio.setText(producto != null && producto.getNombre() != null
                            ? "$ " + producto.getPrecio() : "");
                    verPrecioView.txtCodigo.setText("");
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
            };
            return k;
    }
    
    public Producto obtenerPrecioNombreProducto(){
        return query.obtenerPrecio_nombre(verPrecioView.txtCodigo.getText());
    }
    
    /*Método aplicado a btn cerrar de la ventana de ver precio*/
    private void asignarESCBtnVerCerrar() {
    // Crear y asociar el Action con la tecla ESC
    InputMap inputMap = verPrecioView.btnCerrar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = verPrecioView.btnCerrar.getActionMap();

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pressedESC");
    actionMap.put("pressedESC", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Cerrar la ventana verPrecioView
            verPrecioView.dispose();
        }
    });
}
    
}
