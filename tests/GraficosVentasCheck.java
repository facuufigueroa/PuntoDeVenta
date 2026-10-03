package View;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;

public final class GraficosVentasCheck {
    private static void check(boolean value,String message) { if(!value)throw new AssertionError(message); }
    private static BufferedImage render(GraficoTorta chart) {
        chart.setSize(500,540);chart.doLayout();
        BufferedImage image=new BufferedImage(500,540,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();chart.printAll(g);g.dispose();return image;
    }
    private static int coloredPixels(BufferedImage image) {
        int count=0;
        for(int y=70;y<360;y++)for(int x=100;x<400;x++) {Color c=new Color(image.getRGB(x,y));if(c.getGreen()>c.getRed()+30 && c.getGreen()>c.getBlue()+10)count++;}
        return count;
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Map<String,BigDecimal> data=new LinkedHashMap<>();
            for(int i=1;i<=8;i++)data.put("Producto "+i,BigDecimal.valueOf(i));
            Map<String,BigDecimal> top=GraficosVentasView.principales(data);
            check(top.size()==6 && top.get("Producto 8").intValue()==8,"Top five plus other products");
            BigDecimal sum=BigDecimal.ZERO;for(BigDecimal amount:top.values())sum=sum.add(amount);
            check(sum.intValue()==36,"Grouping preserves total");
            GraficoTorta chart=new GraficoTorta("Medios de pago");chart.datos(Collections.emptyMap());
            check(coloredPixels(render(chart))==0,"Empty period paints without fake slices");
            chart.datos(Collections.singletonMap("Efectivo",new BigDecimal("100")));
            check(coloredPixels(render(chart))>1000,"Single medium paints a full donut");
            check(chart.getAccessibleContext().getAccessibleDescription().contains("Efectivo"),"Chart values accessible as text");
            System.out.println("GraficosVentasCheck OK: grouping, empty and populated charts, accessible values");
        });
    }
}
