package View;

import Config.EmpresaConfig;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Generic code-drawn assets and optional customer logo. */
public final class MarcaEmpresa {
    private MarcaEmpresa(){}
    public static BufferedImage placeholder(int ancho,int alto) {
        BufferedImage image=new BufferedImage(ancho,alto,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(235,245,239));g.fillRoundRect(0,0,ancho,alto,12,12);g.setColor(new Color(22,112,80));
        g.setFont(new Font("SansSerif",Font.BOLD,Math.max(9,alto/5)));
        for(int i=0;i<2;i++){String text=i==0 ? "LOGO" : "EMPRESA";g.drawString(text,(ancho-g.getFontMetrics().stringWidth(text))/2,alto/2+(i==0 ? -3 : g.getFontMetrics().getHeight()-3));}
        g.dispose();return image;
    }
    public static BufferedImage icono() {
        BufferedImage image=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(new Color(22,112,80));g.fillRoundRect(0,0,64,64,16,16);g.setColor(Color.WHITE);g.setStroke(new BasicStroke(3));g.drawRoundRect(15,12,34,40,5,5);g.drawLine(23,23,41,23);g.drawLine(23,31,41,31);g.drawLine(23,39,35,39);g.dispose();return image;
    }
    public static ImageIcon logo(int ancho,int alto) {
        BufferedImage image=null;String path=EmpresaConfig.actual().logo;
        if(!path.isEmpty())try{image=ImageIO.read(new File(path));}catch(Exception e){/* Use the placeholder if a configured file is unavailable. */}
        if(image==null)return new ImageIcon(placeholder(ancho,alto));
        double escala=Math.min((double)ancho/image.getWidth(),(double)alto/image.getHeight());
        return new ImageIcon(image.getScaledInstance(Math.max(1,(int)(image.getWidth()*escala)),Math.max(1,(int)(image.getHeight()*escala)),Image.SCALE_SMOOTH));
    }
    public static JLabel etiquetaLogo(int ancho,int alto) {
        JLabel label=new JLabel(logo(ancho,alto));label.putClientProperty("logoEmpresa",new Dimension(ancho,alto));label.setToolTipText(EmpresaConfig.actual().nombre);return label;
    }
    public static void actualizarVentanas() {
        for(Frame frame:Frame.getFrames())if(frame instanceof JFrame){JFrame vista=(JFrame)frame;Object title=vista.getRootPane().getClientProperty("tituloVista");if(title!=null)vista.setTitle(EmpresaConfig.actual().nombre+" · "+title);actualizar(vista.getContentPane());}
    }
    private static void actualizar(Container root) {
        for(Component c:root.getComponents()){
            if(c instanceof JLabel){JLabel label=(JLabel)c;Object size=label.getClientProperty("logoEmpresa");if(size instanceof Dimension){Dimension d=(Dimension)size;label.setIcon(logo(d.width,d.height));label.setToolTipText(EmpresaConfig.actual().nombre);}if(Boolean.TRUE.equals(label.getClientProperty("pieEmpresa")))label.setText(EmpresaConfig.actual().nombre+" · Sistema de ventas");}
            if(c instanceof Container)actualizar((Container)c);
        }
    }
}
