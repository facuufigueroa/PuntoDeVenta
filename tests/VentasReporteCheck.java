import Caja.CajaService;
import Reporte.ReporteVentas;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRJdk13Compiler;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/** Compile the A4 template for Java 8 and validate it using synthetic sales only. */
public final class VentasReporteCheck {
    private static String texto(JasperPrint reporte) {
        StringBuilder texto=new StringBuilder();
        for(JRPrintPage pagina:reporte.getPages())for(JRPrintElement elemento:pagina.getElements())
            if(elemento instanceof JRPrintText)texto.append(((JRPrintText)elemento).getFullText()).append('\n');
        return texto.toString();
    }
    private static void contiene(JasperPrint reporte,String... esperados) {
        String texto=texto(reporte);for(String esperado:esperados)if(!texto.contains(esperado))throw new AssertionError("Falta: "+esperado);
    }
    private static CajaService.VentaRegistro venta(long id,String medio,String total,boolean anulada) {
        CajaService.VentaRegistro v=new CajaService.VentaRegistro();v.id=id;v.caja=3;v.fecha=Timestamp.valueOf("2026-10-01 10:30:00");v.medio=medio;v.total=new BigDecimal(total);if(anulada)v.anulada=Timestamp.valueOf("2026-10-02 09:00:00");return v;
    }
    public static void main(String[] args) throws Exception {
        JasperReport plantilla=new JRJdk13Compiler(DefaultJasperReportsContext.getInstance()) {
            @Override protected String getCompilerClass(){return JRJdk13Compiler.class.getName();}
            @Override public String compileClasses(File[] files,String classpath) throws JRException {
                List<String> opciones=new ArrayList<>(Arrays.asList("-source","8","-target","8","-classpath",classpath));for(File file:files)opciones.add(file.getAbsolutePath());
                java.io.ByteArrayOutputStream errores=new java.io.ByteArrayOutputStream();
                int resultado=javax.tools.ToolProvider.getSystemJavaCompiler().run(null,errores,errores,opciones.toArray(new String[0]));return resultado==0 ? null : errores.toString();
            }
        }.compileReport(JRXmlLoader.load("src/Reporte/ventas.jrxml"));
        if(plantilla.getPageWidth()!=595 || plantilla.getPageHeight()!=842)throw new AssertionError("Formato A4");
        JRSaver.saveObject(plantilla,"src/Reporte/ventas.jasper");
        LocalDate dia=LocalDate.of(2026,10,1);
        CajaService.Historial h=new CajaService.Historial();
        h.ventas.add(venta(101,"Efectivo","100.50",false));h.ventas.add(venta(102,"Tarjeta","200.00",false));h.ventas.add(venta(103,"Transferencia","50.00",true));
        JasperPrint reporte=ReporteVentas.generar(h,dia,dia,null);
        contiene(reporte,Config.EmpresaConfig.actual().nombre+" · REPORTE DE VENTAS");
        java.text.NumberFormat moneda=java.text.NumberFormat.getCurrencyInstance(new Locale("es","AR"));
        contiene(reporte,"01/10/2026 al 01/10/2026","Medio de pago: Todos","#101","#103","Anulada","Confirmadas: 2  |  Anuladas: 1","TOTAL BRUTO: "+moneda.format(new BigDecimal("350.50")),"TOTAL ANULADO: "+moneda.format(new BigDecimal("50.00")),"TOTAL NETO: "+moneda.format(new BigDecimal("300.50")),"Tarjeta confirmada: "+moneda.format(new BigDecimal("200")));
        CajaService.Historial filtrado=new CajaService.Historial();filtrado.ventas.add(h.ventas.get(1));
        JasperPrint tarjeta=ReporteVentas.generar(filtrado,dia,dia,"Tarjeta");contiene(tarjeta,"Medio de pago: Tarjeta","#102");if(texto(tarjeta).contains("#101"))throw new AssertionError("Filtro mezcla ventas");
        JasperPrint vacio=ReporteVentas.generar(new CajaService.Historial(),dia,dia,null);contiene(vacio,"Sin ventas para los filtros seleccionados.","TOTAL NETO: "+moneda.format(BigDecimal.ZERO));
        CajaService.Historial largo=new CajaService.Historial();for(int i=1;i<=180;i++)largo.ventas.add(venta(i,"Efectivo","1",false));
        JasperPrint paginado=ReporteVentas.generar(largo,dia,dia,null);if(paginado.getPages().size()<2)throw new AssertionError("Falta paginación");contiene(paginado,"#180","TOTAL NETO: "+moneda.format(new BigDecimal("180")),"Página 2");
        new File("build/reporte-ventas").mkdirs();
        JasperExportManager.exportReportToPdfFile(reporte,"build/reporte-ventas/ejemplo.pdf");
        ImageIO.write((java.awt.image.BufferedImage)JasperPrintManager.printPageToImage(reporte,0,1.5f),"png",new File("build/reporte-ventas/ejemplo.png"));
        System.out.println("VentasReporteCheck OK: A4, filtros, anulaciones, totales, período vacío, paginación y PDF");
    }
}
