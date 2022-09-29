package Controller;

import DataBase.Querys;
import View.VentaView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import static java.lang.Integer.parseInt;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public final class VentaController implements ActionListener,KeyListener {
    
    private Querys query = new Querys();
    
    private final VentaView ventaView = new VentaView();
    
    DefaultTableModel modeloVenta = new DefaultTableModel();

    public VentaController() {
        ventaView.txtCodigo.setFocusable(true);
        this.ventaView.txtPagaCon.addKeyListener(accionPagaCon());
        this.ventaView.btnObtenerVuelto.addActionListener(this);
        ventaView.btnNuevaCompra.addActionListener(this);
        this.ventaView.btnAgregarOtro.addActionListener(this);
        this.ventaView.txtCodigo.addKeyListener(accionAgregarATabla());
        iniciarTabla();
        iniciarcomboBoxRapido();
    }
    
       
    public void iniciarTabla(){
        modeloVenta.addColumn("Producto");
        modeloVenta.addColumn("Precio");
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

                int vuelto=pagaCon-preciosin$;

                ventaView.txtVuelto.setText("$ "+String.valueOf(vuelto));
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
        String botones[] = {"Aceptar", "Cancelar"};
        
        if(e.getSource() == ventaView.btnNuevaCompra){
            int eleccion = JOptionPane.showOptionDialog(null, "<html><p style = \"font:15px\">¿Esta seguro de generar nueva compra?</p></html>", "Nueva Compra", 0, 0, null, botones, this);
            if (eleccion == JOptionPane.YES_OPTION) {
                modeloVenta.setRowCount(0);
                vaciarTextFields();
                ventaView.txtCodigo.requestFocus();
            }
           
        }
       
    }
    
    public void vaciarTextFields(){
        ventaView.txtTotalAPagar.setText("");
        ventaView.txtPagaCon.setText("");
        ventaView.txtVuelto.setText("");
        ventaView.cbbNombre.setSelectedItem("");
        ventaView.txtPrecio.setText(null);
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
        String[] productosRapidos={"","Carnes","Pan","Alimento"};
        for(String items : productosRapidos){
            ventaView.cbbNombre.addItem(items);
        }
        
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
    
    
    
    
}
