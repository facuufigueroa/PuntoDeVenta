package Config;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Per-installation business identity; never contains database credentials. */
public final class EmpresaConfig {
    public final String nombre,direccion,contacto,mensaje,logo;
    private static volatile EmpresaConfig actual=cargar(Paths.get("config/empresa.properties"));
    public EmpresaConfig(String nombre,String direccion,String contacto,String mensaje,String logo) {
        this.nombre=validar(nombre,80,true);this.direccion=validar(direccion,120,false);this.contacto=validar(contacto,100,false);this.mensaje=validar(mensaje,120,false);this.logo=logo==null ? "" : logo.trim();
    }
    private static String validar(String valor,int limite,boolean requerido) {
        String s=valor==null ? "" : valor.trim();
        if((requerido && s.isEmpty()) || s.length()>limite || s.contains("\n") || s.contains("\r"))throw new IllegalArgumentException("Revisá los datos del negocio: nombre obligatorio (hasta 80 caracteres), dirección y mensaje hasta 120, contacto hasta 100.");
        return s;
    }
    public static EmpresaConfig actual(){return actual;}
    public static EmpresaConfig cargar(Path archivo) {
        Properties p=new Properties();
        if(Files.exists(archivo))try(Reader reader=Files.newBufferedReader(archivo,java.nio.charset.StandardCharsets.UTF_8)){p.load(reader);}catch(IOException e){System.err.println("No se pudo leer la identidad del negocio. Se usan los datos genéricos.");}
        try{return new EmpresaConfig(p.getProperty("nombre","MI EMPRESA"),p.getProperty("direccion",""),p.getProperty("contacto",""),p.getProperty("mensaje","¡Gracias por tu compra!"),p.getProperty("logo",""));}
        catch(IllegalArgumentException e){return new EmpresaConfig("MI EMPRESA","","","¡Gracias por tu compra!","");}
    }
    public static synchronized void guardar(EmpresaConfig datos) throws IOException {
        actual=guardar(Paths.get("config/empresa.properties"),datos);
    }
    public static EmpresaConfig guardar(Path archivo,EmpresaConfig datos) throws IOException {
        archivo=archivo.toAbsolutePath().normalize();Files.createDirectories(archivo.getParent());
        String logo="";
        if(!datos.logo.isEmpty()) {
            Path origen=Paths.get(datos.logo).toAbsolutePath().normalize();
            java.awt.image.BufferedImage imagen=ImageIO.read(origen.toFile());
            if(imagen==null || imagen.getWidth()>8000 || imagen.getHeight()>8000)throw new IOException("Elegí un logo PNG o JPG válido, de hasta 8000 píxeles por lado.");
            Path destino=archivo.getParent().resolve("empresa-logo.png");
            Path temporal=Files.createTempFile(archivo.getParent(),"logo-",".png");
            try {ImageIO.write(imagen,"png",temporal.toFile());mover(temporal,destino);}finally{Files.deleteIfExists(temporal);}
            Path raiz=Paths.get(".").toAbsolutePath().normalize();
            logo=destino.startsWith(raiz) ? raiz.relativize(destino).toString() : destino.toString();
        }
        EmpresaConfig guardada=new EmpresaConfig(datos.nombre,datos.direccion,datos.contacto,datos.mensaje,logo);
        Properties p=new Properties();p.setProperty("nombre",guardada.nombre);p.setProperty("direccion",guardada.direccion);p.setProperty("contacto",guardada.contacto);p.setProperty("mensaje",guardada.mensaje);p.setProperty("logo",logo);
        Path temporal=Files.createTempFile(archivo.getParent(),"empresa-",".tmp");
        try {try(Writer writer=Files.newBufferedWriter(temporal,java.nio.charset.StandardCharsets.UTF_8)){p.store(writer,"Datos del negocio");}mover(temporal,archivo);}finally{Files.deleteIfExists(temporal);}
        return guardada;
    }
    private static void mover(Path desde,Path hasta)throws IOException {
        try{Files.move(desde,hasta,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(desde,hasta,StandardCopyOption.REPLACE_EXISTING);}
    }
    public Map<String,Object> parametros() {
        Map<String,Object> p=new HashMap<>();p.put("empresa",nombre);p.put("direccion",direccion);p.put("contacto",contacto);p.put("mensaje",mensaje);return p;
    }
}
