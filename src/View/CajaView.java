package View;

import Caja.CajaService;
import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class CajaView extends JFrame {
    private final CajaService service = new CajaService();
    private final JLabel estado = new JLabel("Cargando caja...");
    private final JLabel totales = new JLabel();
    private final DefaultTableModel movimientos = model("Fecha", "Tipo", "Medio", "Monto", "Detalle");
    private final DefaultTableModel sesiones = model("Caja", "Apertura", "Cierre", "Fondo", "Esperado", "Contado", "Diferencia");
    private final JButton abrir = new JButton(), ingreso = new JButton(), retiro = new JButton(), cerrar = new JButton(), actualizar = new JButton();
    private long id;
    private final Runnable alAbrir;
    public CajaView() {this(null);}
    public CajaView(Runnable alAbrir) {
        this.alAbrir=alAbrir;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = Estilo.shell(this, "Control de caja", "Abrí el turno, registrá movimientos y conciliá el efectivo.");
        JPanel body = new JPanel(new BorderLayout(12,12));
        body.setOpaque(false); estado.setFont(new Font("Segoe UI", Font.BOLD,20));
        JPanel resumen = new JPanel(new GridLayout(2,1,0,8)); resumen.setOpaque(false); resumen.add(estado); resumen.add(totales); body.add(resumen,BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Movimientos de caja abierta",new JScrollPane(new JTable(movimientos)));
        tabs.addTab("Historial de cierres",new JScrollPane(new JTable(sesiones)));
        body.add(tabs); root.add(body);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton b : new JButton[]{abrir,ingreso,retiro,cerrar,actualizar}) actions.add(b);
        Estilo.button(abrir,"Abrir caja",true); Estilo.button(ingreso,"Ingreso",false); Estilo.button(retiro,"Retiro",false); Estilo.button(cerrar,"Cerrar caja",true); Estilo.button(actualizar,"Actualizar",false);
        root.add(actions,BorderLayout.SOUTH);
        abrir.addActionListener(e -> { BigDecimal m = pedir("Fondo inicial en efectivo"); if(m!=null) run(() -> service.abrir(m),true); });
        ingreso.addActionListener(e -> movimiento("INGRESO")); retiro.addActionListener(e -> movimiento("RETIRO"));
        cerrar.addActionListener(e -> {
            BigDecimal m = pedir("Efectivo contado al cierre");
            if (m != null && ConfirmacionView.confirmar(this,"¿Cerrar caja?","Se guardará el efectivo contado y la diferencia. El turno quedará cerrado.","Cerrar caja")) {
                long actual = id; run(() -> service.cerrar(actual,m));
            }
        });
        actualizar.addActionListener(e -> run(() -> {},false,false));
        setSize(1000,650); setLocationRelativeTo(null); run(() -> {},false,false);
    }
    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns,0) { public boolean isCellEditable(int r,int c) { return false; } };
    }
    private BigDecimal pedir(String title) {
        String value = JOptionPane.showInputDialog(this,title);
        if (value == null) return null;
        try { return CajaService.importe(value); } catch (IllegalArgumentException e) { JOptionPane.showMessageDialog(this,e.getMessage()); return null; }
    }
    private void movimiento(String tipo) {
        BigDecimal m = pedir(tipo.equals("INGRESO") ? "Efectivo que ingresa" : "Efectivo que se retira");
        if(m==null) return;
        String detalle = JOptionPane.showInputDialog(this,"Motivo del movimiento");
        if(detalle!=null) run(() -> service.movimiento(tipo,"Efectivo",m,detalle));
    }
    private interface Work { void run() throws Exception; }
    private void run(Work work) {
        run(work,false,true);
    }
    private void run(Work work,boolean apertura) {run(work,apertura,true);}
    private void run(Work work,boolean apertura,boolean avisar) {
        enabled(false);
        new SwingWorker<CajaService.Resumen,Void>() {
            protected CajaService.Resumen doInBackground() throws Exception { work.run(); return service.resumen(); }
            protected void done() {
                try {
                    CajaService.Resumen r = get(); id=r.id;
                    estado.setText(id==0 ? "Caja cerrada" : "Caja #"+id+" · Efectivo esperado: $ "+r.efectivo);
                    totales.setText("Ventas del turno · Efectivo: $"+r.ventas.get("Efectivo")+" · Tarjeta: $"+r.ventas.get("Tarjeta")+" · Transferencia: $"+r.ventas.get("Transferencia"));
                    movimientos.setRowCount(0); sesiones.setRowCount(0);
                    for(Object[] row:r.movimientos) movimientos.addRow(row);
                    for(Object[] row:r.sesiones) sesiones.addRow(row);
                    enabled(true);
                    if(avisar)SonidosWindows.exito();
                    if(apertura && alAbrir!=null && id!=0) {dispose();alAbrir.run();}
                } catch(Exception e) {
                    SonidosWindows.error();
                    estado.setText("No se pudo actualizar la caja"); id=0;
                    JOptionPane.showMessageDialog(CajaView.this,"No se pudo completar o consultar la operación. Actualizá antes de repetirla.\n"+(e.getCause()==null ? e.getMessage() : e.getCause().getMessage())+"\nSi es la primera vez, importá database/caja.sql.");
                    actualizar.setEnabled(true);
                }
            }
        }.execute();
    }
    private void enabled(boolean ready) {
        abrir.setEnabled(ready && id==0); ingreso.setEnabled(ready && id!=0); retiro.setEnabled(ready && id!=0); cerrar.setEnabled(ready && id!=0); actualizar.setEnabled(ready);
    }
}
