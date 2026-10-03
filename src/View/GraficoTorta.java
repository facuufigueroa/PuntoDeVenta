package View;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;

/** Vector donut and a readable legend, rendered without external libraries. */
final class GraficoTorta extends JPanel {
    private static final Color[] COLORS={new Color(22,112,80),new Color(54,133,181),new Color(232,164,53),new Color(137,105,182),new Color(215,104,104),new Color(91,153,144)};
    private final String titulo;
    private final LinkedHashMap<String,BigDecimal> datos=new LinkedHashMap<>();
    private final NumberFormat moneda=NumberFormat.getCurrencyInstance(new Locale("es","AR"));
    private final JPanel leyenda=new JPanel();
    private final Lienzo dibujo=new Lienzo();
    GraficoTorta(String titulo) {
        super(new BorderLayout(12,12)); this.titulo=titulo;
        setBackground(Color.WHITE); setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(219,228,223)),BorderFactory.createEmptyBorder(18,18,18,18)));
        JLabel heading=new JLabel(titulo); heading.setFont(new Font("Segoe UI",Font.BOLD,19)); add(heading,BorderLayout.NORTH);
        dibujo.setPreferredSize(new Dimension(320,280)); dibujo.setOpaque(false); add(dibujo);
        leyenda.setLayout(new BoxLayout(leyenda,BoxLayout.Y_AXIS)); leyenda.setOpaque(false);
        JScrollPane scroll=new JScrollPane(leyenda); scroll.setBorder(null); scroll.setPreferredSize(new Dimension(320,155)); add(scroll,BorderLayout.SOUTH);
        getAccessibleContext().setAccessibleName(titulo);
    }
    void datos(Map<String,BigDecimal> valores) {
        datos.clear(); for(Map.Entry<String,BigDecimal> entry:valores.entrySet()) if(entry.getValue().signum()>0) datos.put(entry.getKey(),entry.getValue());
        BigDecimal total=total(); leyenda.removeAll(); int i=0;
        for(Map.Entry<String,BigDecimal> entry:datos.entrySet()) {
            Color color=COLORS[i++%COLORS.length];
            String porcentaje=String.format(new Locale("es","AR"),"%.1f%%",entry.getValue().doubleValue()/total.doubleValue()*100);
            JLabel label=new JLabel(entry.getKey()+" · "+moneda.format(entry.getValue())+" · "+porcentaje);
            label.setIcon(new Icon() { public int getIconWidth(){return 16;} public int getIconHeight(){return 16;} public void paintIcon(Component c,Graphics g,int x,int y){g.setColor(color);g.fillOval(x+2,y+2,12,12);} });
            label.setFont(new Font("Segoe UI",Font.PLAIN,13)); label.setBorder(BorderFactory.createEmptyBorder(4,0,4,0)); label.setToolTipText(label.getText()); leyenda.add(label);
        }
        getAccessibleContext().setAccessibleDescription(titulo+": "+datos.toString()); revalidate(); repaint();
    }
    private BigDecimal total() { BigDecimal sum=BigDecimal.ZERO; for(BigDecimal value:datos.values()) sum=sum.add(value); return sum; }
    private final class Lienzo extends JPanel {
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics); Graphics2D g=(Graphics2D)graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int size=Math.max(0,Math.min(getWidth(),getHeight())-24), x=(getWidth()-size)/2,y=(getHeight()-size)/2;
                BigDecimal total=total(); double start=90; int i=0;
                if(total.signum()==0) { g.setColor(new Color(234,239,236)); g.fillOval(x,y,size,size); }
                else for(BigDecimal value:datos.values()) { double angle=360*value.doubleValue()/total.doubleValue(); g.setColor(COLORS[i++%COLORS.length]); g.fill(new Arc2D.Double(x,y,size,size,start,-angle,Arc2D.PIE)); start-=angle; }
                int hole=(int)(size*.65); g.setColor(Color.WHITE); g.fillOval((getWidth()-hole)/2,(getHeight()-hole)/2,hole,hole);
                g.setColor(new Color(31,47,42)); g.setFont(new Font("Segoe UI",Font.BOLD,16)); String text=total.signum()==0 ? "Sin ventas" : moneda.format(total);
                FontMetrics metrics=g.getFontMetrics(); g.drawString(text,(getWidth()-metrics.stringWidth(text))/2,getHeight()/2+6);
            } finally { g.dispose(); }
        }
    }
}
