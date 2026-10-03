package Reporte;

import Caja.CajaService;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

/** A4 sales ledger generated from one filtered history snapshot. */
public final class ReporteVentas {
    private ReporteVentas() {}
    public static JasperPrint generar(CajaService.Historial historial,LocalDate desde,LocalDate hasta,String medio) throws JRException {
        NumberFormat moneda=NumberFormat.getCurrencyInstance(new Locale("es","AR"));
        Map<String,Object> parametros=new HashMap<>();
        parametros.putAll(Config.EmpresaConfig.actual().parametros());
        parametros.put("periodo",desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))+" al "+hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parametros.put("medio",medio==null ? "Todos" : medio);
        parametros.put("generado",new Date());
        BigDecimal bruto=BigDecimal.ZERO,anulado=BigDecimal.ZERO;
        Map<String,BigDecimal> netos=new LinkedHashMap<>();for(String m:CajaService.MEDIOS)netos.put(m,BigDecimal.ZERO);
        int anuladas=0;
        Collection<Map<String,?>> filas=new ArrayList<>();
        for(CajaService.VentaRegistro venta:historial.ventas) {
            Map<String,Object> fila=new HashMap<>();
            fila.put("venta","#"+venta.id);fila.put("fecha",venta.fecha);fila.put("caja",String.valueOf(venta.caja));
            fila.put("medio",venta.medio);fila.put("estado",venta.anulada==null ? "Confirmada" : "Anulada");fila.put("importe",moneda.format(venta.total));filas.add(fila);
            bruto=bruto.add(venta.total);
            if(venta.anulada!=null) {anuladas++;anulado=anulado.add(venta.total);}
            else netos.merge(venta.medio,venta.total,BigDecimal::add);
        }
        parametros.put("cantidad",historial.ventas.size());parametros.put("confirmadas",historial.ventas.size()-anuladas);parametros.put("anuladas",anuladas);
        parametros.put("bruto",moneda.format(bruto));parametros.put("anulado",moneda.format(anulado));parametros.put("neto",moneda.format(bruto.subtract(anulado)));
        parametros.put("efectivo",moneda.format(netos.get("Efectivo")));parametros.put("tarjeta",moneda.format(netos.get("Tarjeta")));parametros.put("transferencia",moneda.format(netos.get("Transferencia")));
        java.net.URL plantilla=ReporteVentas.class.getResource("/Reporte/ventas.jasper");
        if(plantilla==null)throw new JRException("No se encontró la plantilla Reporte/ventas.jasper.");
        JasperReport reporte=(JasperReport)JRLoader.loadObject(plantilla);
        return JasperFillManager.fillReport(reporte,parametros,new JRMapCollectionDataSource(filas));
    }
    public static void mostrar(JasperPrint reporte) {
        JasperViewer vista=new JasperViewer(reporte,false);
        vista.setTitle(Config.EmpresaConfig.actual().nombre+" · Reporte de ventas");vista.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);vista.setVisible(true);
    }
}
