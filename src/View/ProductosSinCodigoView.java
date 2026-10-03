package View;

import DataBase.ProductosSinCodigoService;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class ProductosSinCodigoView extends JFrame {
    private final ProductosSinCodigoService service=new ProductosSinCodigoService();
    private final Runnable alCambiar;
    private final JTextField nombre=new JTextField();
    private final JButton guardar=new JButton(),nuevo=new JButton(),activar=new JButton(),actualizar=new JButton();
    private final JLabel estado=new JLabel("Cargando opciones...");
    private final DefaultTableModel modelo=new DefaultTableModel(new String[]{"ID","Nombre","Estado"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable tabla=new JTable(modelo);
    private Long id;
    private boolean activo,busy;
    public ProductosSinCodigoView(Runnable alCambiar) {
        this.alCambiar=alCambiar;setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root=Estilo.shell(this,"Productos sin código","Administrá las opciones rápidas de venta. El precio se ingresa al vender.");
        JPanel body=new JPanel(new BorderLayout(14,14));body.setOpaque(false);
        JPanel editor=new JPanel(new BorderLayout(10,8));editor.setOpaque(false);editor.add(new JLabel("Nombre"),BorderLayout.NORTH);editor.add(nombre);
        JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));botones.setOpaque(false);botones.add(Estilo.button(guardar,"Guardar",true));botones.add(Estilo.button(nuevo,"Nuevo",false));editor.add(botones,BorderLayout.SOUTH);body.add(editor,BorderLayout.NORTH);
        tabla.setRowHeight(34);tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);tabla.setAutoCreateRowSorter(true);tabla.getTableHeader().setReorderingAllowed(false);body.add(new JScrollPane(tabla));body.add(estado,BorderLayout.SOUTH);root.add(body);
        JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));acciones.setOpaque(false);acciones.add(Estilo.button(activar,"Desactivar",false));acciones.add(Estilo.button(actualizar,"Actualizar",false));root.add(acciones,BorderLayout.SOUTH);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if(e.getValueIsAdjusting() || busy)return;
            int row=tabla.getSelectedRow();if(row<0){limpiar();return;}row=tabla.convertRowIndexToModel(row);
            id=((Number)modelo.getValueAt(row,0)).longValue();nombre.setText((String)modelo.getValueAt(row,1));activo="Activo".equals(modelo.getValueAt(row,2));habilitar();
        });
        nuevo.addActionListener(e -> {tabla.clearSelection();limpiar();nombre.requestFocusInWindow();});
        guardar.addActionListener(e -> {Long seleccion=id;String valor=nombre.getText();run(() -> service.guardar(seleccion,valor),true);});
        activar.addActionListener(e -> {
            if(id==null)return;
            boolean siguiente=!activo;String accion=siguiente ? "Activar" : "Desactivar";
            if(ConfirmacionView.confirmar(this,"¿"+accion+" opción?",siguiente ? "Volverá a aparecer en la lista de ventas." : "Dejará de aparecer en la lista. Los carritos y tickets ya registrados se conservan.",accion)) {
                long seleccion=id;run(() -> service.activar(seleccion,siguiente),true);
            }
        });
        actualizar.addActionListener(e -> run(() -> {},false));
        setSize(850,650);setMinimumSize(new Dimension(750,550));setLocationRelativeTo(null);run(() -> {},false);
    }
    private void limpiar(){id=null;nombre.setText("");activo=false;habilitar();}
    private interface Work{void run()throws Exception;}
    private void run(Work work,boolean cambio) {
        if(busy)return;busy=true;habilitar();
        new SwingWorker<java.util.List<ProductosSinCodigoService.Entrada>,Void>() {
            protected java.util.List<ProductosSinCodigoService.Entrada> doInBackground()throws Exception{work.run();return service.listar(false);}
            protected void done(){
                try {
                    java.util.List<ProductosSinCodigoService.Entrada> lista=get();modelo.setRowCount(0);
                    for(ProductosSinCodigoService.Entrada e:lista)modelo.addRow(new Object[]{e.id,e.nombre,e.activo ? "Activo" : "Inactivo"});
                    estado.setText(lista.size()+" opciones · Seleccioná una para editarla o cambiar su estado.");limpiar();
                    if(cambio)SonidosWindows.exito();
                    if(cambio && alCambiar!=null)alCambiar.run();
                }catch(Exception e){SonidosWindows.error();estado.setText("No se pudo actualizar la lista.");JOptionPane.showMessageDialog(ProductosSinCodigoView.this,"No se pudo completar o consultar la operación. Actualizá antes de repetirla.\n"+(e.getCause()==null ? e.getMessage() : e.getCause().getMessage())+"\nSi es la primera vez, importá database/productos-sin-codigo.sql.");}
                finally{busy=false;habilitar();}
            }
        }.execute();
    }
    private void habilitar(){nombre.setEnabled(!busy);guardar.setEnabled(!busy);nuevo.setEnabled(!busy);actualizar.setEnabled(!busy);tabla.setEnabled(!busy);activar.setEnabled(!busy && id!=null);activar.setText(activo ? "Desactivar" : "Activar");}
}
