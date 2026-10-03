package View;

import Config.EmpresaConfig;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class EmpresaView extends JFrame {
    private final JTextField nombre=new JTextField(),direccion=new JTextField(),contacto=new JTextField(),mensaje=new JTextField(),logo=new JTextField();
    private final JButton guardar=new JButton(),elegir=new JButton(),quitar=new JButton(),conexion=new JButton();
    public EmpresaView(){
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);JPanel root=Estilo.shell(this,"Configuración del negocio","Personalizá el sistema y los comprobantes para tu empresa.");
        JPanel campos=new JPanel(new GridLayout(0,1,8,8));campos.setOpaque(false);
        EmpresaConfig datos=EmpresaConfig.actual();nombre.setText(datos.nombre);direccion.setText(datos.direccion);contacto.setText(datos.contacto);mensaje.setText(datos.mensaje);logo.setText(datos.logo);logo.setEditable(false);
        agregar(campos,"Nombre del negocio",nombre);agregar(campos,"Dirección (opcional)",direccion);agregar(campos,"Teléfono / contacto (opcional)",contacto);agregar(campos,"Mensaje del ticket",mensaje);agregar(campos,"Logo (opcional)",logo);root.add(campos);
        JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));botones.setOpaque(false);botones.add(Estilo.button(elegir,"Elegir logo",false));botones.add(Estilo.button(quitar,"Usar LOGO EMPRESA",false));botones.add(Estilo.button(guardar,"Guardar configuración",true));root.add(botones,BorderLayout.SOUTH);
        elegir.addActionListener(e -> {JFileChooser file=new JFileChooser();file.setFileFilter(new FileNameExtensionFilter("Logo PNG o JPG","png","jpg","jpeg"));if(file.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)logo.setText(file.getSelectedFile().getAbsolutePath());});
        quitar.addActionListener(e -> logo.setText(""));
        botones.add(Estilo.button(conexion,"Conexión MySQL",false));conexion.addActionListener(e -> NavegacionVentanas.abrir(this,new ConexionView(null)));
        guardar.addActionListener(e -> guardar());setSize(800,690);setMinimumSize(new Dimension(730,650));setLocationRelativeTo(null);
    }
    private void agregar(JPanel root,String titulo,JTextField field){JPanel p=new JPanel(new BorderLayout(0,5));p.setOpaque(false);p.add(new JLabel(titulo),BorderLayout.NORTH);p.add(field);root.add(p);}
    private void guardar(){
        final EmpresaConfig datos;
        try{datos=new EmpresaConfig(nombre.getText(),direccion.getText(),contacto.getText(),mensaje.getText(),logo.getText());}catch(IllegalArgumentException e){JOptionPane.showMessageDialog(this,e.getMessage());return;}
        guardar.setEnabled(false);elegir.setEnabled(false);quitar.setEnabled(false);
        new SwingWorker<Void,Void>(){
            protected Void doInBackground()throws Exception{EmpresaConfig.guardar(datos);return null;}
            protected void done(){try{get();logo.setText(EmpresaConfig.actual().logo);MarcaEmpresa.actualizarVentanas();SonidosWindows.exito();JOptionPane.showMessageDialog(EmpresaView.this,"Configuración guardada. Los próximos tickets y reportes usarán estos datos.");}catch(Exception e){SonidosWindows.error();JOptionPane.showMessageDialog(EmpresaView.this,"No se pudo guardar: "+(e.getCause()==null ? e.getMessage() : e.getCause().getMessage()));}finally{guardar.setEnabled(true);elegir.setEnabled(true);quitar.setEnabled(true);}}
        }.execute();
    }
}
