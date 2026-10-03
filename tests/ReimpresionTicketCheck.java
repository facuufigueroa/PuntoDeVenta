import Model.Compra;
import java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

/** Fill the existing ticket with historical products and date, without changing artifacts. */
public final class ReimpresionTicketCheck {
    public static void main(String[] args) throws Exception {
        JasperReport report=(JasperReport)JRLoader.loadObject(new java.io.File("src/Reporte/ticket.jasper"));
        Map<String,Object> parameters=new HashMap<>();
        parameters.put("total","$30.50");
        parameters.put("fecha",java.sql.Timestamp.valueOf("2020-02-03 10:15:00"));
        JasperPrint print=JasperFillManager.fillReport(report,parameters,new JRBeanCollectionDataSource(Arrays.asList(new Compra("Pan histórico","$10.00"),new Compra("Leche histórica","$20.50"))));
        StringBuilder text=new StringBuilder();
        for(JRPrintPage page:print.getPages()) for(JRPrintElement element:page.getElements())
            if(element instanceof JRPrintText) text.append(((JRPrintText)element).getFullText()).append('\n');
        for(String expected:new String[]{"Pan histórico","Leche histórica","$30.50","03/02/2020","10:15"})
            if(text.indexOf(expected)<0) throw new AssertionError("Missing historical ticket value: "+expected);
        System.out.println("ReimpresionTicketCheck OK: original date, products and total");
    }
}
