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
    private boolean cobrando;
    private ArrayList<Compra> ultimoTicket;
    private String ultimoTotal;
    private String solicitudCobro, firmaCobro;
    private final View.AccesoCaja accesoCaja=new View.AccesoCaja();
    public View.AccesoCaja getAccesoCaja() {return accesoCaja;}
    private boolean cargandoSinCodigo;
    private boolean avisoSinCodigo;
    

    public VentaController() {
        ventaView.cbbNombre.addItem("");
        ventaView.btnSinCodigo.addActionListener(event -> administrarSinCodigo());
        ventaView.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowActivated(java.awt.event.WindowEvent event) {iniciarcomboBoxRapido();}
        });
        ventaView.btnCobrar.addActionListener(event -> confirmarCobro());
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
    }

    private void confirmarCobro() {
        if(cobrando)return;
        if(modeloVenta.getRowCount()==0) {View.SonidosWindows.error();JOptionPane.showMessageDialog(ventaView,"Agregá productos antes de cobrar.");return;}
        accesoCaja.prepararVenta(ventaView,() -> ejecutarCobro());
    }
    private void ejecutarCobro() {
        if (cobrando) return;
        try {
            if (modeloVenta.getRowCount() == 0) throw new IllegalArgumentException("Agregá productos antes de cobrar.");
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            StringBuilder detalle = new StringBuilder();
            for (int i=0; i<modeloVenta.getRowCount(); i++) {
                java.math.BigDecimal precio = Caja.CajaService.importe(modeloVenta.getValueAt(i,1).toString().replace("$", ""));
                total = total.add(precio);
                detalle.append(modeloVenta.getValueAt(i,0)).append(" · $").append(precio).append('\n');
            }
            String medio = (String) ventaView.medioPago.getSelectedItem();
            if (medio.equals("Efectivo")) {
                java.math.BigDecimal recibido = Caja.CajaService.importe(ventaView.txtPagaCon.getText());
                if (recibido.compareTo(total)<0) throw new IllegalArgumentException("El efectivo recibido es menor al total.");
            }
            if (!ConfirmacionView.confirmar(ventaView,"¿Confirmar cobro?","Registrar $"+total+" por "+medio+" en la caja abierta y comenzar una nueva compra.","Cobrar")) return;
            final java.math.BigDecimal monto = total;
            final String items = detalle.toString();
            final ArrayList<Compra> ticket = recorrerJTable();
            String firma = medio+"\n"+items;
            if(!firma.equals(firmaCobro)) { firmaCobro=firma; solicitudCobro=java.util.UUID.randomUUID().toString(); }
            final String solicitud = solicitudCobro;
            cobrando=true;
            habilitarVenta(ventaView.getContentPane(),false);
            new javax.swing.SwingWorker<Void,Void>() {
                protected Void doInBackground() throws Exception {
                    new Caja.CajaService().cobrar(solicitud,medio,ticket); return null;
                }
                protected void done() {
                    try {
                        get(); solicitudCobro=null; firmaCobro=null; ultimoTicket=ticket; ultimoTotal="$"+monto; modeloVenta.setRowCount(0); vaciarTextFields(); ventaView.txtCodigo.setText("");
                        View.SonidosWindows.exito();
                        JOptionPane.showMessageDialog(ventaView,"Cobro registrado por $"+monto+" ("+medio+").");
                    } catch(Exception error) {
                        View.SonidosWindows.error();
                        JOptionPane.showMessageDialog(ventaView,"No se confirmó el cobro. El carrito se conserva.\n"+(error.getCause()==null ? error.getMessage() : error.getCause().getMessage()));
                    } finally {
                        cobrando=false; habilitarVenta(ventaView.getContentPane(),true); ventaView.txtCodigo.requestFocusInWindow();
                    }
                }
            }.execute();
        } catch(IllegalArgumentException error) { View.SonidosWindows.error();JOptionPane.showMessageDialog(ventaView,error.getMessage()); }
    }

    private void habilitarVenta(java.awt.Container container, boolean enabled) {
        for (java.awt.Component component : container.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof java.awt.Container) habilitarVenta((java.awt.Container)component,enabled);
        }
    }
    
       
    public void iniciarTabla(){
        ventaView.tablaProductos.setRowHeight(35);
        ventaView.tablaProductos.setModel(modeloVenta);
       
    }
    
    public void listarEnTabla(String codigo){
        int antes=modeloVenta.getRowCount();
        query.listarProducto(codigo, modeloVenta);
        if(modeloVenta.getRowCount()>antes)View.SonidosWindows.producto();
    }
    
    public void loadVentaView(){
       loadVentaView(ventaView);
    }
    public void loadVentaView(javax.swing.JFrame owner) {
        accesoCaja.prepararVenta(owner,() -> {View.NavegacionVentanas.abrir(owner,ventaView);ventaView.txtCodigo.requestFocusInWindow();});
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
                java.math.BigDecimal pagaCon;
                try { pagaCon = Caja.CajaService.importe(ventaView.txtPagaCon.getText()); }
                catch (IllegalArgumentException error) { JOptionPane.showMessageDialog(ventaView,error.getMessage()); return; }
                
                String precioCon$ = ventaView.txtTotalAPagar.getText();
                int preciosin$ = parseInt(precioCon$.substring(1,precioCon$.length()));
                if(pagaCon.compareTo(java.math.BigDecimal.valueOf(preciosin$)) >= 0){
                    java.math.BigDecimal vuelto=pagaCon.subtract(java.math.BigDecimal.valueOf(preciosin$));

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
                solicitudCobro=null; firmaCobro=null;
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
            if(cargandoSinCodigo || cobrando)return;
            Object seleccionado=ventaView.cbbNombre.isEditable() ? ventaView.cbbNombre.getEditor().getItem() : ventaView.cbbNombre.getSelectedItem();
            String nombre=seleccionado==null ? "" : seleccionado.toString().trim();
            if(!nombre.isEmpty()){
                if(!"".equals(ventaView.txtPrecio.getText())){
                    int precio;
                    try {precio=parseInt(ventaView.txtPrecio.getText().trim());if(precio<=0 || nombre.length()>255)throw new IllegalArgumentException();}
                    catch(IllegalArgumentException error){JOptionPane.showMessageDialog(ventaView,"Ingresá un precio positivo en pesos enteros y un nombre de hasta 255 caracteres.");return;}
                    otro[0]=nombre;
                    otro[1]= "$"+precio;
                    query.listarOtro(modeloVenta, otro);
                    View.SonidosWindows.producto();
                    
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
        if(cargandoSinCodigo || cobrando)return;
        cargandoSinCodigo=true;
        Object seleccionado=ventaView.cbbNombre.isEditable() ? ventaView.cbbNombre.getEditor().getItem() : ventaView.cbbNombre.getSelectedItem();
        boolean manual=ventaView.cbbNombre.getSelectedIndex()<0 || (ventaView.cbbNombre.isEditable() && !java.util.Objects.equals(seleccionado,ventaView.cbbNombre.getSelectedItem()));
        ventaView.cbbNombre.setEnabled(false);ventaView.btnAgregarOtro.setEnabled(false);
        new javax.swing.SwingWorker<java.util.List<DataBase.ProductosSinCodigoService.Entrada>,Void>() {
            protected java.util.List<DataBase.ProductosSinCodigoService.Entrada> doInBackground()throws Exception{return new DataBase.ProductosSinCodigoService().listar(true);}
            protected void done(){
                try {
                    java.util.List<DataBase.ProductosSinCodigoService.Entrada> lista=get();
                    ventaView.cbbNombre.removeAllItems();ventaView.cbbNombre.addItem("");boolean existe=false;
                    for(DataBase.ProductosSinCodigoService.Entrada entrada:lista){ventaView.cbbNombre.addItem(entrada.nombre);if(entrada.nombre.equals(seleccionado))existe=true;}
                    ventaView.cbbNombre.setSelectedItem(existe || manual ? seleccionado : "");
                    ventaView.cbbNombre.setToolTipText("Opciones guardadas en la base. También podés escribir un nombre ocasional.");
                    avisoSinCodigo=false;
                }catch(Exception error){
                    ventaView.cbbNombre.setToolTipText("No se pudo actualizar la lista de productos sin código.");
                    if(ventaView.cbbNombre.getItemCount()<=1 && !avisoSinCodigo){avisoSinCodigo=true;JOptionPane.showMessageDialog(ventaView,"No se pudo cargar la lista de productos sin código. Podés escribir un nombre manualmente.\nSi es la primera vez, importá database/productos-sin-codigo.sql.");}
                }finally{cargandoSinCodigo=false;ventaView.cbbNombre.setEnabled(!cobrando);ventaView.btnAgregarOtro.setEnabled(!cobrando);}
            }
        }.execute();
    }
    public void administrarSinCodigo(){administrarSinCodigo(ventaView);}
    public void administrarSinCodigo(javax.swing.JFrame padre){View.NavegacionVentanas.abrir(padre,new View.ProductosSinCodigoView(() -> iniciarcomboBoxRapido()));}
    
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
        if(ventaView.txtTotalAPagar.getText().length()!=0 && !ventaView.txtPagaCon.getText().trim().isEmpty()){
            java.math.BigDecimal pagaCon;
            try { pagaCon = Caja.CajaService.importe(ventaView.txtPagaCon.getText()); }
            catch (IllegalArgumentException error) { ventaView.txtVuelto.setText(""); return; }
            int precioTotalsin$ = parseInt(ventaView.txtTotalAPagar.getText().substring(1,ventaView.txtTotalAPagar.getText().length()));

            java.math.BigDecimal vuelto=pagaCon.subtract(java.math.BigDecimal.valueOf(precioTotalsin$));
         
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
            
            if (modeloVenta.getRowCount()>0) report.conexionReporte(ventaView.txtTotalAPagar.getText(),recorrerJTable());
            else if (ultimoTicket!=null) report.conexionReporte(ultimoTotal,ultimoTicket);
            else JOptionPane.showMessageDialog(ventaView,"No hay una compra para imprimir.");
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
                
                View.NavegacionVentanas.abrir(ventaView,verPrecioView);
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
