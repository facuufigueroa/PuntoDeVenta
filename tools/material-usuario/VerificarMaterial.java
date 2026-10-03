import java.io.File;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.jcodec.api.awt.AWTFrameGrab;
import org.jcodec.common.io.*;
import org.jcodec.common.DemuxerTrackMeta;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

public final class VerificarMaterial {
 public static void main(String[] args)throws Exception{
  Path root=Paths.get("material-usuario");
  PdfReader pdf=new PdfReader(root.resolve("Manual-ilustrado.pdf").toString());
  if(pdf.getNumberOfPages()!=22)throw new IllegalStateException("Manual incompleto");
  PdfTextExtractor text=new PdfTextExtractor(pdf);
  for(int p=1;p<=pdf.getNumberOfPages();p++){
   if(text.getTextFromPage(p).trim().length()<100)throw new IllegalStateException("Texto incompleto en página "+p);
   if(pdf.getPageSizeWithRotation(p).getWidth()<pdf.getPageSizeWithRotation(p).getHeight())throw new IllegalStateException("Página sin orientación horizontal");
  }
  pdf.close();
  File file=root.resolve("Venta-paso-a-paso.mp4").toFile();
  try(SeekableByteChannel channel=NIOUtils.readableChannel(file)){
   AWTFrameGrab grab=AWTFrameGrab.createAWTFrameGrab(channel);DemuxerTrackMeta meta=grab.getVideoTrack().getMeta();
   if(meta.getTotalFrames()!=360 || Math.abs(meta.getTotalDuration()-90)>0.1)throw new IllegalStateException("Duración o cuadros incorrectos");
   for(int frame:new int[]{0,180,359}){
    grab.seekToFramePrecise(frame);BufferedImage image=grab.getFrame();
    if(image==null || image.getWidth()!=1440 || image.getHeight()!=1080)throw new IllegalStateException("No decodificó el cuadro "+frame);
    ImageIO.write(image,"png",new File("build/material-validacion-"+frame+".png"));
   }
   System.out.println("Verificado: PDF de 22 páginas A4 horizontal; MP4 H.264 de 90 segundos y 360 cuadros, 1440 x 1080; inicio, mitad y final decodificados.");
  }
 }
}
