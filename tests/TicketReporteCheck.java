import Model.Compra;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.design.JRJdk13Compiler;

/** Compile with the same JasperReports 6.0.0 library used by the application. */
public final class TicketReporteCheck {
    private static String texts(JasperPrint print) {
        StringBuilder text = new StringBuilder();
        for (JRPrintPage page : print.getPages())
            for (JRPrintElement element : page.getElements())
                if (element instanceof JRPrintText)
                    text.append(((JRPrintText) element).getFullText()).append('\n');
        return text.toString();
    }

    public static void main(String[] args) throws Exception {
        // Preserve Java 8 compatibility of the expression classes inside the .jasper file.
        JasperReport report = new JRJdk13Compiler(DefaultJasperReportsContext.getInstance()) {
            @Override protected String getCompilerClass(){return JRJdk13Compiler.class.getName();}
            @Override public String compileClasses(File[] files, String classpath) throws JRException {
                List<String> options = new ArrayList<>(Arrays.asList(
                        "-source", "8", "-target", "8", "-classpath", classpath));
                for (File file : files) options.add(file.getAbsolutePath());
                java.io.ByteArrayOutputStream messages = new java.io.ByteArrayOutputStream();
                int result = javax.tools.ToolProvider.getSystemJavaCompiler().run(null, messages, messages,
                        options.toArray(new String[0]));
                return result == 0 ? null : messages.toString();
            }
        }.compileReport(JRXmlLoader.load("src/Reporte/ticket.jrxml"));
        if (report.getPageWidth() != 151) throw new AssertionError("Ticket width changed");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("total", "$ 9700");
        parameters.put("fecha", new Date(1790874000000L));
        List<Compra> rows = Arrays.asList(
                new Compra("Yerba mate selección especial con palo paquete 1 kg", "$4500"),
                new Compra("Leche entera 1 L", "$1600"),
                new Compra("Pan de campo", "$1200"),
                new Compra("Fiambre", "$2400"));
        JasperPrint print = JasperFillManager.fillReport(report, parameters, new JRBeanCollectionDataSource(rows));
        String content = texts(print);
        if (!content.contains("paquete 1 kg") || !content.contains("$ 9700") || !content.contains("Artículos: 4"))
            throw new AssertionError("Missing long name, total or item count");
        List<Compra> many = new ArrayList<>();
        for (int i = 0; i < 80; i++) many.add(new Compra("Producto " + i, "$100"));
        JasperPrint longPrint = JasperFillManager.fillReport(report, parameters, new JRBeanCollectionDataSource(many));
        if (longPrint.getPages().size() < 2 || !texts(longPrint).contains("Producto 79")
                || !texts(longPrint).contains("Artículos: 80"))
            throw new AssertionError("Long tickets lose rows or summary");
        new File("docs/apariencia").mkdirs();
        BufferedImage image = (BufferedImage) JasperPrintManager.printPageToImage(print, 0, 4f);
        ImageIO.write(image, "png", new File("docs/apariencia/ticket.png"));
        JasperExportManager.exportReportToPdfFile(print, "docs/apariencia/ticket.pdf");
        JRSaver.saveObject(report, "src/Reporte/ticket.jasper");
        System.out.println("OK: width 151, long product names, totals and pagination; compiled ticket updated");
    }
}
