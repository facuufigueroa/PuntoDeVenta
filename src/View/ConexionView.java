package View;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;
import javax.swing.*;

/** Initial connection setup; updates apply when the application is next started. */
public final class ConexionView extends JFrame {
    private final JTextField url=new JTextField("jdbc:mysql://localhost:3306/punto_venta"),usuario=new JTextField("root");
    private final JPasswordField password=new JPasswordField();
    private final JButton probar=new JButton(),guardar=new JButton();
    private final Runnable alConfigurar;
    public ConexionView(Runnable alConfigurar){
        this.alConfigurar=alConfigurar;setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root=Estilo.shell(this,"Conexión a la base de datos","Configurá la conexión de esta instalación. Los datos se guardan solo en esta PC.");
        Path archivo=Paths.get("config/database.properties");Properties p=new Properties();
        if(Files.exists(archivo))try(InputStream in=Files.newInputStream(archivo)){p.load(in);url.setText(p.getProperty("url",url.getText()));usuario.setText(p.getProperty("user","root"));password.setText(p.getProperty("password",""));}catch(IOException e){JOptionPane.showMessageDialog(this,"No se pudo leer la configuración actual.");}
        JPanel fields=new JPanel(new GridLayout(0,1,8,8));fields.setOpaque(false);agregar(fields,"URL de MySQL",url);agregar(fields,"Usuario",usuario);agregar(fields,"Contraseña",password);root.add(fields);
        JPanel bottom=new JPanel(new BorderLayout(0,12));bottom.setOpaque(false);
        JLabel nota=new JLabel("Primero importá database/instalar.sql en MySQL. Para una base existente, usá su nombre en la URL.");bottom.add(nota,BorderLayout.NORTH);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));buttons.setOpaque(false);buttons.add(Estilo.button(probar,"Probar conexión",false));buttons.add(Estilo.button(guardar,"Guardar",true));bottom.add(buttons);root.add(bottom,BorderLayout.SOUTH);
        probar.addActionListener(e -> operar(false));guardar.addActionListener(e -> operar(true));setSize(900,550);setMinimumSize(new Dimension(850,500));setLocationRelativeTo(null);
    }
    private void agregar(JPanel root,String title,JTextField input){JPanel p=new JPanel(new BorderLayout(0,5));p.setOpaque(false);p.add(new JLabel(title),BorderLayout.NORTH);p.add(input);root.add(p);}
    private void operar(boolean guardarArchivo){
        String endpoint=url.getText().trim(),user=usuario.getText().trim(),secret=new String(password.getPassword());
        if(!endpoint.startsWith("jdbc:mysql://") || user.isEmpty()){JOptionPane.showMessageDialog(this,"Indicá una URL jdbc:mysql:// y un usuario.");return;}
        probar.setEnabled(false);guardar.setEnabled(false);
        new SwingWorker<Void,Void>(){
            protected Void doInBackground()throws Exception{
                
                try(Connection c=DriverManager.getConnection(endpoint.replaceFirst("^jdbc:mysql:","jdbc:mariadb:"),user,secret)){
                    for(String table:new String[]{"producto","caja_control","caja_sesion","caja_movimiento","venta","venta_item","venta_anulacion","producto_sin_codigo"})
                        try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM "+table+" WHERE 1=0")){/* Checks required tables without changing the database. */}
                }
                if(guardarArchivo){
                    Path archivo=Paths.get("config/database.properties");Files.createDirectories(archivo.getParent());Properties datos=new Properties();datos.setProperty("url",endpoint);datos.setProperty("user",user);datos.setProperty("password",secret);
                    Path temp=Files.createTempFile(archivo.getParent(),"conexion-",".tmp");try{try(OutputStream out=Files.newOutputStream(temp)){datos.store(out,"Conexion local del punto de venta");}Files.move(temp,archivo,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temp);}
                }return null;
            }
            protected void done(){
                try{get();if(guardarArchivo){JOptionPane.showMessageDialog(ConexionView.this,alConfigurar==null ? "Conexión guardada. Reiniciá la aplicación para usarla." : "Conexión guardada.");dispose();if(alConfigurar!=null)alConfigurar.run();}else JOptionPane.showMessageDialog(ConexionView.this,"Conexión correcta. Todas las tablas están disponibles.");}
                catch(Exception e){JOptionPane.showMessageDialog(ConexionView.this,"No se pudo verificar la conexión y las tablas. Revisá MySQL, los datos ingresados y la instalación de database/instalar.sql. No se guardó ningún cambio.");}
                finally{probar.setEnabled(true);guardar.setEnabled(true);}
            }
        }.execute();
    }
}
