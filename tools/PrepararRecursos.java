import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRJdk13Compiler;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/** Build-time resource generation; no database or customer configuration is accessed. */
public final class PrepararRecursos {
    public static void main(String[] args)throws Exception{
        JRJdk13Compiler compiler=new JRJdk13Compiler(DefaultJasperReportsContext.getInstance()){
            @Override protected String getCompilerClass(){return JRJdk13Compiler.class.getName();}
            @Override public String compileClasses(File[] files,String classpath)throws JRException{
                List<String> opciones=new ArrayList<>(Arrays.asList("-source","8","-target","8","-classpath",classpath));for(File file:files)opciones.add(file.getAbsolutePath());java.io.ByteArrayOutputStream errores=new java.io.ByteArrayOutputStream();
                int status=javax.tools.ToolProvider.getSystemJavaCompiler().run(null,errores,errores,opciones.toArray(new String[0]));return status==0 ? null : errores.toString();
            }
        };
        for(String nombre:new String[]{"ticket","ventas"})JRSaver.saveObject(compiler.compileReport(JRXmlLoader.load("src/Reporte/"+nombre+".jrxml")),"src/Reporte/"+nombre+".jasper");
        ImageIO.write(View.MarcaEmpresa.placeholder(180,110),"png",new File("src/Imagenes/logo-empresa.png"));
        ImageIO.write(View.MarcaEmpresa.icono(),"png",new File("src/Imagenes/icon-app.png"));
        System.out.println("Plantillas e identidad genérica preparadas.");
    }
}
