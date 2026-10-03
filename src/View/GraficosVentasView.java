package View;

import Caja.CajaService;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;

public final class GraficosVentasView extends JFrame {
    private final CajaService service=new CajaService();
    private final SelectorFecha desde=new SelectorFecha(LocalDate.now()),hasta=new SelectorFecha(LocalDate.now());
    private final JButton actualizar=new JButton(),hoy=new JButton();
    private final JLabel resumen=new JLabel("Cargando gráficos...");
    private final GraficoTorta medios=new GraficoTorta("Ventas por medio de pago"),productos=new GraficoTorta("Productos por importe vendido");
    public GraficosVentasView(LocalDate inicio,LocalDate fin) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { desde.cleanup();hasta.cleanup(); }
        });
        desde.setFecha(inicio); hasta.setFecha(fin);
        JPanel root=Estilo.shell(this,"Gráficos de ventas","Importes de ventas confirmadas del período seleccionado.");
        JPanel body=new JPanel(new BorderLayout(16,16)); body.setOpaque(false);
        JPanel filtros=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); filtros.setOpaque(false);
        filtros.add(new JLabel("Desde")); filtros.add(desde); filtros.add(new JLabel("Hasta")); filtros.add(hasta);
        filtros.add(Estilo.button(actualizar,"Actualizar",true)); filtros.add(Estilo.button(hoy,"Hoy",false)); body.add(filtros,BorderLayout.NORTH);
        JPanel graficos=new JPanel(new GridLayout(1,2,18,0)); graficos.setOpaque(false); graficos.add(medios); graficos.add(productos); body.add(graficos);
        resumen.setFont(new Font("Segoe UI",Font.BOLD,14)); body.add(resumen,BorderLayout.SOUTH); root.add(body);
        root.add(new JLabel("Productos: los 5 de mayor importe y el resto agrupado. Las ventas anuladas se muestran aparte."),BorderLayout.SOUTH);
        actualizar.addActionListener(e -> cargar()); hoy.addActionListener(e -> {desde.setFecha(LocalDate.now());hasta.setFecha(desde.getFecha());cargar();});
        setSize(1120,750);setMinimumSize(new Dimension(950,680));setLocationRelativeTo(null);cargar();
    }
    static Map<String,BigDecimal> principales(Map<String,BigDecimal> datos) {
        java.util.List<Map.Entry<String,BigDecimal>> entries=new ArrayList<>(datos.entrySet());
        entries.sort((a,b) -> b.getValue().compareTo(a.getValue()));
        Map<String,BigDecimal> result=new LinkedHashMap<>(); BigDecimal resto=BigDecimal.ZERO;
        for(int i=0;i<entries.size();i++) { Map.Entry<String,BigDecimal> entry=entries.get(i); if(i<5) result.put(entry.getKey(),entry.getValue()); else resto=resto.add(entry.getValue()); }
        if(resto.signum()>0) { String label="Otros productos (resto)"; while(result.containsKey(label)) label+=" "; result.put(label,resto); }
        return result;
    }
    private void cargar() {
        final LocalDate inicio,fin;
        try { inicio=desde.getFecha();fin=hasta.getFecha();if(fin.isBefore(inicio)) throw new IllegalArgumentException(); }
        catch(RuntimeException e) { JOptionPane.showMessageDialog(this,"La fecha Hasta debe ser igual o posterior a Desde.");return; }
        habilitar(false);
        new SwingWorker<CajaService.Estadisticas,Void>() {
            protected CajaService.Estadisticas doInBackground() throws Exception { return service.estadisticas(inicio,fin); }
            protected void done() {
                try {
                    CajaService.Estadisticas datos=get(); medios.datos(datos.medios);productos.datos(principales(datos.productos));
                    BigDecimal promedio=datos.confirmadas==0 ? BigDecimal.ZERO : datos.total.divide(BigDecimal.valueOf(datos.confirmadas),2,java.math.RoundingMode.HALF_UP);
                    resumen.setText(datos.confirmadas+" ventas confirmadas · Total: $"+datos.total+" · Ticket promedio: $"+promedio+" · "+datos.anuladas+" anuladas ($"+datos.importeAnulado+")");
                } catch(Exception e) { resumen.setText("No se pudieron actualizar los gráficos. Los datos visibles corresponden a la consulta anterior."); JOptionPane.showMessageDialog(GraficosVentasView.this,"No se pudieron consultar las ventas.\n"+(e.getCause()==null ? e.getMessage() : e.getCause().getMessage())); }
                finally { habilitar(true); }
            }
        }.execute();
    }
    private void habilitar(boolean enabled) { desde.setEnabled(enabled);hasta.setEnabled(enabled);actualizar.setEnabled(enabled);hoy.setEnabled(enabled); }
}
