package View;

import Caja.CajaService;
import Caja.CajaService.VentaRegistro;
import java.awt.*;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Read-only ledger with explicit, audited sale reversals. */
public final class HistorialVentasView extends JFrame {
    private final CajaService service = new CajaService();
    private final SelectorFecha desde = new SelectorFecha(LocalDate.now()), hasta = new SelectorFecha(LocalDate.now());
    private final JComboBox<String> medio = new JComboBox<>(new String[]{"Todos","Efectivo","Tarjeta","Transferencia"});
    private final JButton buscar = new JButton(), hoy = new JButton(), detalle = new JButton(), imprimir = new JButton(), anular = new JButton();
    private final JLabel totales = new JLabel("Cargando ventas...");
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Venta", "Fecha", "Caja", "Medio", "Total", "Estado"},0) {
        public boolean isCellEditable(int r,int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private boolean busy;
    private final JButton graficos=new JButton();
    private final JButton reporte=new JButton();

    public HistorialVentasView() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { desde.cleanup();hasta.cleanup(); }
        });
        JPanel root = Estilo.shell(this,"Historial de ventas","Consultá compras, recuperá tickets y registrá devoluciones.");
        JPanel body = new JPanel(new BorderLayout(12,16)); body.setOpaque(false);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); filtros.setOpaque(false);
        filtros.add(new JLabel("Desde")); filtros.add(desde);
        filtros.add(new JLabel("Hasta")); filtros.add(hasta); filtros.add(medio);
        filtros.add(Estilo.button(buscar,"Buscar",true)); filtros.add(Estilo.button(hoy,"Hoy",false));
        filtros.add(Estilo.button(reporte,"Reporte de ventas",false));
        reporte.addActionListener(e -> imprimirReporte());
        body.add(filtros,BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setRowHeight(34); tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setReorderingAllowed(false); body.add(new JScrollPane(tabla));
        totales.setFont(new Font("Segoe UI",Font.BOLD,15)); body.add(totales,BorderLayout.SOUTH); root.add(body);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); acciones.setOpaque(false);
        acciones.add(Estilo.button(detalle,"Ver detalle",false)); acciones.add(Estilo.button(imprimir,"Reimprimir ticket",false)); acciones.add(Estilo.button(anular,"Anular venta",false)); root.add(acciones,BorderLayout.SOUTH);
        acciones.add(Estilo.button(graficos,"Gráficos de ventas",true));
        graficos.addActionListener(e -> {
            try {
                LocalDate inicio=desde.getFecha(),fin=hasta.getFecha();
                if(fin.isBefore(inicio)) throw new IllegalArgumentException();
                NavegacionVentanas.abrir(this,new GraficosVentasView(inicio,fin));
            } catch(RuntimeException error) { JOptionPane.showMessageDialog(this,"Revisá el rango de fechas antes de abrir los gráficos."); }
        });
        buscar.addActionListener(e -> cargar());
        hoy.addActionListener(e -> { desde.setFecha(LocalDate.now()); hasta.setFecha(desde.getFecha()); medio.setSelectedIndex(0); cargar(); });
        detalle.addActionListener(e -> seleccion(this::mostrarDetalle));
        imprimir.addActionListener(e -> seleccion(v -> {
            if(v.anulada!=null) { JOptionPane.showMessageDialog(this,"La venta está anulada. Consultá su detalle y el motivo."); return; }
            if(v.items.isEmpty()) { JOptionPane.showMessageDialog(this,"No se puede reconstruir el ticket: el detalle guardado no contiene productos con precios verificables que coincidan con el total. Consultá Ver detalle."); return; }
            new Reporte.Reporte().conexionReporte("$"+v.total.toPlainString(),v.items,v.fecha);
        }));
        anular.addActionListener(e -> seleccion(this::pedirAnulacion));
        tabla.getSelectionModel().addListSelectionListener(e -> habilitar());
        setSize(1100,650); setMinimumSize(new Dimension(1000,550)); setLocationRelativeTo(null); cargar();
    }
    private void cargar() {
        if(busy) return;
        try {
            LocalDate inicio=desde.getFecha(), fin=hasta.getFecha();
            if(fin.isBefore(inicio)) throw new IllegalArgumentException();
            String filtro=medio.getSelectedIndex()==0 ? null : (String)medio.getSelectedItem();
            ejecutar(() -> service.historial(inicio,fin,filtro), r -> {
                modelo.setRowCount(0);
                for(VentaRegistro v:r.ventas) modelo.addRow(new Object[]{v.id,v.fecha,v.caja,v.medio,v.total,v.anulada==null ? "Confirmada" : "Anulada"});
                totales.setText(r.ventas.size()+" ventas · Bruto: $"+r.bruto+" · Anulado: $"+r.anulado+" · Neto: $"+r.bruto.subtract(r.anulado));
            });
        } catch(RuntimeException e) { JOptionPane.showMessageDialog(this,"La fecha Hasta debe ser igual o posterior a Desde."); }
    }
    private void imprimirReporte() {
        if(busy)return;
        final LocalDate inicio,fin;
        try {
            inicio=desde.getFecha();fin=hasta.getFecha();
            if(fin.isBefore(inicio))throw new IllegalArgumentException();
        } catch(RuntimeException e) {JOptionPane.showMessageDialog(this,"La fecha Hasta debe ser igual o posterior a Desde.");return;}
        final String filtro=medio.getSelectedIndex()==0 ? null : (String)medio.getSelectedItem();
        ejecutar(() -> Reporte.ReporteVentas.generar(service.historial(inicio,fin,filtro),inicio,fin,filtro),Reporte.ReporteVentas::mostrar);
    }
    private void seleccion(Consumer<VentaRegistro> next) {
        int row=tabla.getSelectedRow(); if(row<0 || busy) return;
        long id=((Number)modelo.getValueAt(tabla.convertRowIndexToModel(row),0)).longValue();
        ejecutar(() -> service.detalleVenta(id),next);
    }
    private void mostrarDetalle(VentaRegistro v) {
        JDialog dialog=new JDialog(this,"Venta #"+v.id,true);
        JPanel content=new JPanel(new BorderLayout(12,12)); content.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JTextArea resumen=new JTextArea("Venta #"+v.id+" · "+v.fecha+" · Caja #"+v.caja+"\n"+v.medio+" · Total: $"+v.total+"\n"+(v.anulada==null ? "Confirmada" : "Anulada el "+v.anulada+"\nMotivo: "+v.motivo));
        resumen.setEditable(false); resumen.setLineWrap(true); resumen.setWrapStyleWord(true); resumen.setRows(v.anulada==null ? 3 : 6); content.add(new JScrollPane(resumen),BorderLayout.NORTH);
        if(v.items.isEmpty()) {
            JTextArea original=new JTextArea("Detalle original del cobro; no fue posible recuperar sus productos y precios.\n\n"+v.detalle); original.setEditable(false); original.setLineWrap(true); original.setWrapStyleWord(true); content.add(new JScrollPane(original));
        } else {
            DefaultTableModel items=new DefaultTableModel(new String[]{"Producto","Precio"},0) { public boolean isCellEditable(int r,int c) { return false; } };
            for(Model.Compra item:v.items) items.addRow(new Object[]{item.getNombre(),item.getPrecio()});
            JTable productos=new JTable(items); productos.setRowHeight(30); content.add(new JScrollPane(productos));
        }
        JButton cerrar=new JButton("Cerrar"); cerrar.addActionListener(e -> dialog.dispose()); content.add(cerrar,BorderLayout.SOUTH);
        dialog.setContentPane(content); dialog.setSize(700,500); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }
    private void pedirAnulacion(VentaRegistro v) {
        if(v.anulada!=null) { JOptionPane.showMessageDialog(this,"Esta venta ya está anulada."); return; }
        String motivo=JOptionPane.showInputDialog(this,"Motivo de la anulación de venta #"+v.id);
        if(motivo==null) return;
        if(motivo.trim().isEmpty() || motivo.trim().length()>1000) { JOptionPane.showMessageDialog(this,"Indicá un motivo de entre 1 y 1000 caracteres."); return; }
        if(!ConfirmacionView.confirmar(this,"¿Anular venta #"+v.id+"?","Se registrará una devolución de $"+v.total+" por "+v.medio+" en la caja actualmente abierta. El registro original y los cierres anteriores se conservarán.","Anular venta")) return;
        ejecutar(() -> { service.anular(v.id,motivo); return v.id; }, id -> { JOptionPane.showMessageDialog(this,"Venta #"+id+" anulada. Devolución registrada en caja."); cargar(); });
    }
    private <T> void ejecutar(Callable<T> work,Consumer<T> success) {
        if(busy) return; busy=true; habilitar();
        new SwingWorker<T,Void>() {
            protected T doInBackground() throws Exception { return work.call(); }
            protected void done() {
                busy=false;
                try { T result=get(); success.accept(result); }
                catch(Exception e) { JOptionPane.showMessageDialog(HistorialVentasView.this,"No se pudo completar la operación. Actualizá antes de repetirla.\n"+(e.getCause()==null ? e.getMessage() : e.getCause().getMessage())+"\nEn la primera instalación, importá database/ventas.sql después de caja.sql."); }
                finally { habilitar(); }
            }
        }.execute();
    }
    private void habilitar() {
        reporte.setEnabled(!busy);
        graficos.setEnabled(!busy);
        buscar.setEnabled(!busy); hoy.setEnabled(!busy); desde.setEnabled(!busy); hasta.setEnabled(!busy); medio.setEnabled(!busy); tabla.setEnabled(!busy);
        boolean selected=!busy && tabla.getSelectedRow()>=0;
        detalle.setEnabled(selected); imprimir.setEnabled(selected); anular.setEnabled(selected);
    }
}
