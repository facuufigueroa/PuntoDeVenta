import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.awt.Color;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Generates the customer booklet from its editable Markdown source. */
public final class CrearManual {
    static final Color GREEN=new Color(22,112,80), DARK=new Color(31,48,42), MUTED=new Color(92,107,100);
    static Font font(float size,int style,Color color){return new Font(Font.HELVETICA,size,style,color);}
    static String esc(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    public static void main(String[] args)throws Exception{
        String source=new String(Files.readAllBytes(Paths.get("docs/manual-usuario.md")),StandardCharsets.UTF_8).replace("\uFEFF","");
        String[] pages=source.split("---page---");
        Path pdf=Paths.get("docs/Manual-de-usuario.pdf");
        Document doc=new Document(PageSize.A5,33,33,42,38);
        PdfWriter writer=PdfWriter.getInstance(doc,Files.newOutputStream(pdf));
        writer.setViewerPreferences(PdfWriter.PageLayoutSinglePage);
        writer.setPageEvent(new PdfPageEventHelper(){
            public void onEndPage(PdfWriter w,Document d){
                PdfContentByte cb=w.getDirectContent();
                cb.setColorStroke(new Color(211,224,217));cb.setLineWidth(.6f);
                cb.moveTo(33,30);cb.lineTo(d.getPageSize().getWidth()-33,30);cb.stroke();
                ColumnText.showTextAligned(cb,Element.ALIGN_LEFT,new Phrase("PUNTO DE VENTA  /  MANUAL DE USUARIO",font(7,Font.NORMAL,MUTED)),33,19,0);
                ColumnText.showTextAligned(cb,Element.ALIGN_RIGHT,new Phrase(String.format("%02d",w.getPageNumber()),font(8,Font.BOLD,GREEN)),d.getPageSize().getWidth()-33,19,0);
                if(w.getPageNumber()==1){
                    cb.setColorFill(new Color(235,245,239));cb.roundRectangle(33,80,354,100,8);cb.fill();
                    cb.setColorStroke(GREEN);cb.setLineWidth(2);cb.roundRectangle(53,97,40,66,4);cb.stroke();
                    for(int y=146;y>=115;y-=10){cb.moveTo(62,y);cb.lineTo(y==116?78:84,y);cb.stroke();}
                    ColumnText.showTextAligned(cb,Element.ALIGN_LEFT,new Phrase("TU NEGOCIO, PASO A PASO",font(12,Font.BOLD,GREEN)),111,146,0);
                    ColumnText.showTextAligned(cb,Element.ALIGN_LEFT,new Phrase("Desde la primera venta hasta el cierre.",font(10,Font.NORMAL,DARK)),111,123,0);
                    ColumnText.showTextAligned(cb,Element.ALIGN_LEFT,new Phrase("Formato A5 · Listo para imprimir",font(9,Font.NORMAL,MUTED)),111,105,0);
                }
                if(w.getPageNumber()>1)ColumnText.showTextAligned(cb,Element.ALIGN_LEFT,new Phrase("GUÍA DE USO  /  EDICIÓN 1.0",font(7,Font.BOLD,GREEN)),33,d.getPageSize().getHeight()-25,0);
            }
        });
        doc.addTitle("Punto de Venta - Manual de usuario");doc.addSubject("Guía de operación del sistema de ventas y caja");doc.addAuthor("Punto de Venta");
        doc.open();
        StringBuilder html=new StringBuilder("<!doctype html><html lang='es'><meta charset='utf-8'><title>Manual de usuario · Punto de Venta</title><style>"+
            "*{box-sizing:border-box}body{margin:0;background:#e6eee9;color:#1f302a;font-family:Arial,sans-serif}.page{width:148mm;min-height:210mm;background:white;margin:24px auto;padding:15mm 12mm 13mm;position:relative;box-shadow:0 3px 16px #0001}h1{font-size:27px;line-height:1.12;color:#167050;margin:0 0 22px}h2{font-size:15px;color:#167050;margin:20px 0 8px}p{font-size:12px;line-height:1.55;margin:0 0 9px}.cover h1{font-size:46px;margin-top:30mm}.cover h2{font-size:24px}.foot{position:absolute;bottom:6mm;left:12mm;right:12mm;border-top:1px solid #d3e0d9;padding-top:6px;display:flex;justify-content:space-between;font-size:9px;color:#5c6b64}@page{size:A5;margin:0}@media print{body{background:white}.page{margin:0;box-shadow:none;height:210mm;break-after:page}.page:last-child{break-after:auto}}"+
            "</style><body>");
        for(int i=0;i<pages.length;i++){
            if(i>0)doc.newPage();
            html.append("<section class='page ").append(i==0?"cover":"").append("'>");
            if(i==0){Paragraph gap=new Paragraph(" ");gap.setSpacingAfter(62);doc.add(gap);}
            for(String line:pages[i].split("\\r?\\n")){
                line=line.trim();if(line.isEmpty())continue;
                if(line.startsWith("# ")){
                    String title=line.substring(2);
                    Paragraph p=new Paragraph(title,font(i==0?34:24,Font.BOLD,GREEN));p.setLeading(i==0?37:27);p.setSpacingAfter(14);doc.add(p);
                    new PdfOutline(writer.getRootOutline(),new PdfDestination(PdfDestination.FIT),title);
                    html.append("<h1>").append(esc(title)).append("</h1>");
                }else if(line.startsWith("## ")){
                    Paragraph p=new Paragraph(line.substring(3),font(i==0?19:12,Font.BOLD,GREEN));p.setLeading(16);p.setSpacingBefore(9);p.setSpacingAfter(6);doc.add(p);
                    html.append("<h2>").append(esc(line.substring(3))).append("</h2>");
                }else{
                    boolean label=i==0 && (line.contains("VENTAS ·")||line.equals("LOGO EMPRESA"));
                    Paragraph p=new Paragraph(line,font(label?10:10.2f,label?Font.BOLD:Font.NORMAL,label?GREEN:DARK));p.setLeading(12.5f);p.setSpacingAfter(5);
                    doc.add(p);html.append("<p>").append(esc(line)).append("</p>");
                }
            }
            if(i==0)html.append("<div style=\"margin-top:45px;background:#ebf5ef;border-radius:8px;padding:24px;color:#167050\"><strong>TU NEGOCIO, PASO A PASO</strong><p style=\"margin:10px 0 0\">Desde la primera venta hasta el cierre.<br>Formato A5 · Listo para imprimir</p></div>");
            html.append("<footer class='foot'><span>PUNTO DE VENTA / MANUAL DE USUARIO</span><span>").append(String.format("%02d",i+1)).append("</span></footer></section>");
        }
        doc.close();html.append("</body></html>");Files.write(Paths.get("docs/Manual-de-usuario.html"),html.toString().getBytes(StandardCharsets.UTF_8));
        PdfReader reader=new PdfReader(pdf.toString());
        if(reader.getNumberOfPages()!=pages.length)throw new IllegalStateException("El contenido excedió las páginas previstas: "+reader.getNumberOfPages());
        PdfTextExtractor extractor=new PdfTextExtractor(reader);
        for(int i=1;i<=pages.length;i++){
            String text=extractor.getTextFromPage(i);
            String last=Arrays.stream(pages[i-1].trim().split("\\r?\\n")).filter(s->!s.trim().isEmpty()).reduce((a,b)->b).get().trim();
            if(!text.replaceAll("\\s+"," ").contains(last.replaceAll("\\s+"," ")))throw new IllegalStateException("Texto incompleto en página "+i);
        }
        reader.close();System.out.println("Manual listo: "+pages.length+" páginas A5, texto completo verificado, PDF y HTML.");
    }
}
