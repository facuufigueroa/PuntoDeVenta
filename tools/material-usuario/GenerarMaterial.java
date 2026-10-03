import View.*;
import Controller.*;
import Caja.CajaService;
import Backup.BackupService;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import javax.imageio.ImageIO;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.*;
import org.jcodec.api.awt.AWTSequenceEncoder;

/** Customer training artifacts. Uses ONLY a disposable in-memory database. */
public final class GenerarMaterial {
    static final Path OUT=Paths.get("material-usuario");
    static final Color GREEN=new Color(22,112,80), INK=new Color(31,48,42), RED=new Color(208,48,60);
    static final List<Scene> scenes=new ArrayList<>();
    static MenuPrincipalView menu;static VentaController controller;static VentaView venta;static CajaView caja;
    static String dialogMode="";static javax.swing.Timer dialogs;static AtomicReference<Throwable> failure=new AtomicReference<>();
    static final class Mark implements java.io.Serializable {private static final long serialVersionUID=1L;Rectangle r;String text;Mark(Rectangle r,String t){this.r=r;this.text=t;}}
    static final class Scene implements java.io.Serializable {private static final long serialVersionUID=1L;String id,title,text;transient BufferedImage image;List<Mark> marks=new ArrayList<>();boolean manual,video;}
    static final class Target {Component component;String text;Target(Component c,String s){component=c;text=s;}}
    static Target target(Component c,String s){return new Target(c,s);}
    static void ui(Runnable work)throws Exception{SwingUtilities.invokeAndWait(work);if(failure.get()!=null)throw new RuntimeException(failure.get());}
    static void waitFor(BooleanSupplier ready)throws Exception{
        long end=System.currentTimeMillis()+15000;AtomicBoolean ok=new AtomicBoolean();
        while(System.currentTimeMillis()<end){ui(()->ok.set(ready.getAsBoolean()));if(ok.get())return;Thread.sleep(80);}
        throw new IllegalStateException("La pantalla no terminó de cargar.");
    }
    static Component content(Window w){return w instanceof RootPaneContainer?((RootPaneContainer)w).getContentPane():w;}
    static BufferedImage screenshot(Window w){Component c=content(w);BufferedImage im=new BufferedImage(c.getWidth(),c.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,im.getWidth(),im.getHeight());c.printAll(g);g.dispose();return im;}
    static void scene(String id,String title,String text,Window w,boolean manual,boolean video,Target... targets){
        try{
            Scene s=new Scene();s.id=id;s.title=title;s.text=text;s.manual=manual;s.video=video;s.image=screenshot(w);int ox=0,oy=0;
            if(w instanceof JDialog && venta!=null && venta.isVisible()){
                BufferedImage dialog=s.image;s.image=screenshot(venta);ox=(s.image.getWidth()-dialog.getWidth())/2;oy=(s.image.getHeight()-dialog.getHeight())/2;
                Graphics2D g=s.image.createGraphics();g.setColor(new Color(0,0,0,95));g.fillRect(0,0,s.image.getWidth(),s.image.getHeight());g.drawImage(dialog,ox,oy,null);g.dispose();
            }
            for(Target t:targets){Rectangle r=SwingUtilities.convertRectangle(t.component.getParent(),t.component.getBounds(),content(w));r.translate(ox,oy);s.marks.add(new Mark(r,t.text));}
            ImageIO.write(s.image,"png",OUT.resolve("capturas/"+id+".png").toFile());scenes.add(s);System.out.println("Captura: "+id);
        }catch(Exception e){failure.set(e);throw new RuntimeException(e);}
    }
    static List<Component> components(Container root){List<Component> out=new ArrayList<>();for(Component c:root.getComponents()){out.add(c);if(c instanceof Container)out.addAll(components((Container)c));}return out;}
    static JButton button(Window w,String prefix){for(Component c:components((Container)content(w)))if(c instanceof JButton && ((JButton)c).getText()!=null && (((JButton)c).getText().startsWith(prefix) || (prefix.equals("Aceptar") && ((JButton)c).getText().equals("OK")) || (prefix.equals("Cancelar") && ((JButton)c).getText().equals("Cancel"))))return (JButton)c;throw new IllegalArgumentException("Botón no encontrado: "+prefix);}
    static JButton printer(Window w){for(Component c:components((Container)content(w)))if(c instanceof JButton){String tip=((JButton)c).getToolTipText();if(tip!=null && (tip.toLowerCase().contains("imprim")||tip.toLowerCase().contains("print")))return (JButton)c;}throw new IllegalStateException("No se encontró el botón de impresora");}
    static <T extends Component>T first(Window w,Class<T> type){for(Component c:components((Container)content(w)))if(type.isInstance(c))return type.cast(c);throw new IllegalArgumentException("Control no encontrado: "+type);}
    static Object field(Object owner,String name){try{java.lang.reflect.Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(owner);}catch(Exception e){throw new RuntimeException(e);}}
    static void show(JFrame w,int width,int height){w.setSize(width,height);w.setLocation(0,0);w.setVisible(true);w.validate();}
    static void enter(JTextField field){KeyEvent event=new KeyEvent(field,KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,KeyEvent.VK_ENTER,'\n');for(KeyListener l:field.getKeyListeners())l.keyPressed(event);}
    static void initDatabase()throws Exception{
        String url=System.getenv("PDV_DB_URL");if(url==null || !url.startsWith("jdbc:hsqldb:mem:material_usuario"))throw new IllegalStateException("Esta herramienta solo admite la base de demo en memoria.");
        java.lang.reflect.Field identity=Config.EmpresaConfig.class.getDeclaredField("actual");identity.setAccessible(true);identity.set(null,new Config.EmpresaConfig("MI EMPRESA","","","¡Gracias por tu compra!",""));
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        try(Connection c=DriverManager.getConnection(url,"SA","");Statement st=c.createStatement()){
            st.execute("SET DATABASE SQL SYNTAX MYS TRUE");
            st.execute("CREATE TABLE producto (idproducto INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,codigo VARCHAR(255) UNIQUE,nombre VARCHAR(255),precio INTEGER)");
            st.execute("INSERT INTO producto(codigo,nombre,precio) VALUES ('779000000001','Yerba mate 500 g',1500),('779000000002','Leche entera 1 L',1000),('779000000003','Galletitas de vainilla',800)");
            st.execute("CREATE TABLE producto_sin_codigo(id BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,nombre VARCHAR(255) UNIQUE,activo BOOLEAN DEFAULT TRUE)");
            st.execute("INSERT INTO producto_sin_codigo(nombre) VALUES('Pan'),('Fiambre'),('Frutas y verduras')");
            st.execute("CREATE TABLE caja_control(id INTEGER PRIMARY KEY)");st.execute("INSERT INTO caja_control VALUES(1)");
            st.execute("CREATE TABLE caja_sesion(id BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,cierre TIMESTAMP,fondo DECIMAL(14,2),esperado DECIMAL(14,2),contado DECIMAL(14,2))");
            st.execute("CREATE TABLE caja_movimiento(id BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,sesion BIGINT,fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,tipo VARCHAR(10),medio VARCHAR(20),monto DECIMAL(14,2),detalle VARCHAR(4000))");
            st.execute("CREATE TABLE venta(id BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,movimiento BIGINT UNIQUE NOT NULL,solicitud VARCHAR(36) UNIQUE NOT NULL)");
            st.execute("CREATE TABLE venta_item(id BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,venta BIGINT,nombre VARCHAR(255),precio DECIMAL(14,2))");
            st.execute("CREATE TABLE venta_anulacion(movimiento BIGINT PRIMARY KEY,ajuste BIGINT UNIQUE NOT NULL,fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,motivo VARCHAR(1000))");
            st.execute("ALTER USER SA SET PASSWORD 'demo'");
        }
    }
    static void handleDialogs(){
        try{for(Window w:Window.getWindows())if(w instanceof JDialog && w.isVisible()){
            if(w instanceof ConfirmacionView){
                if(!dialogMode.equals("cobrar"))continue;
                JButton accept=((JDialog)w).getRootPane().getDefaultButton();
                scene("09-confirmar","Confirmá el cobro","Verificá el importe y el medio. Tocá Cobrar para guardar la venta. Cancelar vuelve al carrito.",w,true,true,target(accept,"Cobrar guarda la operación"));
                dialogMode="resultado";accept.doClick(0);return;
            }
            JOptionPane pane=null;for(Component c:components((Container)content(w)))if(c instanceof JOptionPane)pane=(JOptionPane)c;
            if(pane==null)continue;
            if(dialogMode.equals("fondo")){
                JTextField input=first(w,JTextField.class);input.setText("10000");
                JButton accept=button(w,"Aceptar");
                scene("02-fondo","Indicá el efectivo inicial","Escribí el efectivo que hay en el cajón antes de vender. En este ejemplo: $10.000. Aceptar abre el turno.",w,true,true,target(input,"Fondo inicial"),target(accept,"Aceptar"));
                dialogMode="";accept.doClick(0);return;
            }
            String message=String.valueOf(pane.getMessage());
            if(dialogMode.equals("resultado") && message.contains("Cobro registrado")){
                JButton accept=button(w,"Aceptar");
                scene("10-resultado","Venta guardada","Esperá el mensaje Cobro registrado. El carrito se vacía y queda preparado para otro cliente.",w,true,true,target(accept,"Aceptar la confirmación"));
                dialogMode="";accept.doClick(0);return;
            }
            if(dialogMode.equals("cierre")){
                JTextField input=first(w,JTextField.class);input.setText("13000");
                scene("19-cierre","Cerrar el turno","Contá el efectivo real e ingresá el importe. Aceptar y la confirmación siguiente cierran el turno. En esta demo cancelamos para conservar la caja de ejemplo.",w,true,false,target(input,"Efectivo contado"),target(button(w,"Aceptar"),"Continuar al cierre"));
                dialogMode="";button(w,"Cancelar").doClick(0);return;
            }
            failure.compareAndSet(null,new IllegalStateException("Diálogo inesperado en la demo: "+message));w.dispose();return;
        }}catch(Throwable e){failure.compareAndSet(null,e);for(Window w:Window.getWindows())if(w instanceof JDialog)w.dispose();}
    }
    static void captureWorkflow()throws Exception{
        ui(()->{
            dialogs=new javax.swing.Timer(120,e->handleDialogs());dialogs.start();
            menu=new MenuPrincipalView();controller=new VentaController();venta=controller.ventaView;
            menu.btnPuntoDeVenta.addActionListener(e->controller.loadVentaView(menu));
            show(menu,1180,780);
            scene("00-menu","Ubicate en el menú principal","1 Iniciar venta: atender a un cliente. 2 Control de caja: abrir o cerrar el turno. 3 Historial: buscar ventas y tickets.",menu,true,true,target(menu.btnPuntoDeVenta,"Iniciar venta"),target(menu.btnCaja,"Control de caja"),target(menu.btnHistorial,"Historial de ventas"));
            caja=new CajaView();show(caja,1120,720);
        });
        waitFor(()->button(caja,"Actualizar").isEnabled());
        ui(()->{scene("01-caja","Antes de vender, abrí caja","1 Abrir caja inicia el turno. 2 Ingreso y Retiro registran movimientos de efectivo. 3 Cerrar caja permite conciliar al terminar.",caja,true,true,target(button(caja,"Abrir caja"),"Abrir caja"),target(button(caja,"Ingreso"),"Ingreso / Retiro"),target(button(caja,"Cerrar caja"),"Cerrar caja"));dialogMode="fondo";button(caja,"Abrir caja").doClick(0);});
        waitFor(()->dialogMode.isEmpty() && button(caja,"Cerrar caja").isEnabled());
        if(new CajaService().estado().id==0)throw new IllegalStateException("No abrió la caja de demo");
        ui(()->{caja.dispose();menu.setVisible(true);scene("02b-iniciar","Con caja abierta, iniciá la venta","Tocá Iniciar venta. La caja ya tiene el fondo inicial registrado; ahora podés atender al cliente.",menu,false,true,target(menu.btnPuntoDeVenta,"Iniciar venta"));menu.btnPuntoDeVenta.doClick(0);});
        waitFor(()->venta.isVisible() && venta.btnAgregarOtro.isEnabled());
        ui(()->{
            show(venta,1340,1030);venta.txtCodigo.setText("779000000001");
            scene("03-codigo","Agregá el primer producto","Escaneá el código o escribilo en Código de barras. Después presioná Enter. El producto debe estar registrado en el catálogo.",venta,true,true,target(venta.txtCodigo,"Código de barras + Enter"));enter(venta.txtCodigo);
            scene("04-carrito","Revisá el carrito","El producto aparece con su precio. Repetí el código y Enter para cada artículo; controlá la lista y el total antes de cobrar.",venta,true,true,target(venta.tablaProductos,"Artículos agregados"),target(venta.txtTotalAPagar,"Total acumulado"));
            venta.txtCodigo.setText("779000000002");enter(venta.txtCodigo);venta.cbbNombre.setSelectedItem("Pan");venta.txtPrecio.setText("500");
            scene("05-sin-codigo","Agregá un producto sin código","1 Elegí o escribí el nombre. 2 Indicá el precio en pesos enteros. 3 Tocá Agregar al carrito. Usamos Pan por $500.",venta,true,true,target(venta.cbbNombre,"Nombre"),target(venta.txtPrecio,"Precio"),target(venta.btnAgregarOtro,"Agregar al carrito"));venta.btnAgregarOtro.doClick(0);
            if(venta.tablaProductos.getRowCount()!=3 || !venta.txtTotalAPagar.getText().equals("$3000"))throw new IllegalStateException("Carrito incorrecto");
            venta.medioPago.setSelectedItem("Efectivo");venta.txtPagaCon.setText("5000");
            scene("06-pago","Elegí el pago y el dinero recibido","1 Elegí Efectivo. 2 Escribí 5000 en Paga con. 3 Tocá Calcular vuelto o presioná Enter. Para tarjeta o transferencia, elegí ese medio.",venta,true,true,target(venta.medioPago,"Medio de pago"),target(venta.txtPagaCon,"Paga con"),target(venta.btnObtenerVuelto,"Calcular vuelto"));venta.btnObtenerVuelto.doClick(0);
            scene("07-vuelto","Revisá el vuelto","La compra totaliza $3.000 y el cliente entrega $5.000: devolvé $2.000. Calcular el vuelto todavía no guarda una venta.",venta,true,true,target(venta.txtTotalAPagar,"Total: $3.000"),target(venta.txtVuelto,"Vuelto: $2.000"));
            scene("08-cobrar","Tocá Confirmar cobro","Confirmar cobro, o F10, registra la venta. Imprimir o calcular vuelto por sí solos no guardan el cobro.",venta,false,true,target(venta.btnCobrar,"Confirmar cobro / F10"));dialogMode="cobrar";venta.btnCobrar.doClick(0);
        });
        waitFor(()->dialogMode.isEmpty() && venta.btnCobrar.isEnabled() && venta.tablaProductos.getRowCount()==0);
        CajaService service=new CajaService();CajaService.Historial history=service.historial(LocalDate.now(),LocalDate.now(),null);
        if(history.ventas.size()!=1 || history.bruto.compareTo(new BigDecimal("3000"))!=0)throw new IllegalStateException("La demo no registró exactamente una venta de $3.000");
        ui(()->{
            scene("11-imprimir","Abrí el ticket de la venta","Con el carrito vacío, Imprimir ticket recupera el último cobro de esta ventana. No vuelve a cobrar al cliente.",venta,true,true,target(venta.btnImprimir,"Imprimir ticket / F9"));venta.btnImprimir.doClick(0);
        });
        waitFor(()->Arrays.stream(Window.getWindows()).anyMatch(w->w instanceof net.sf.jasperreports.view.JasperViewer && w.isVisible()));
        ui(()->{
            JFrame ticket=(JFrame)Arrays.stream(Window.getWindows()).filter(w->w instanceof net.sf.jasperreports.view.JasperViewer && w.isVisible()).findFirst().get();show(ticket,1050,850);
            scene("12-ticket","Imprimí el comprobante","En el visor, el ícono de impresora permite elegir la impresora y las páginas. El ticket es un comprobante interno. Cerrá el visor para continuar.",ticket,true,true,target(printer(ticket),"Impresora del visor"));
            ticket.dispose();venta.btnVerPrecio.doClick(0);
        });
        waitFor(()->Arrays.stream(Window.getWindows()).anyMatch(w->w instanceof VerPrecio && w.isVisible()));
        ui(()->{
            VerPrecio price=(VerPrecio)Arrays.stream(Window.getWindows()).filter(w->w instanceof VerPrecio && w.isVisible()).findFirst().get();price.txtCodigo.setText("779000000001");enter(price.txtCodigo);if(!price.labelPrecio.getText().contains("1500"))throw new IllegalStateException("Consulta de precio incorrecta");
            scene("13-precio","Consultá un precio sin vender","1 Escribí o escaneá el código y Enter. 2 Mirá el precio. 3 Cerrar vuelve a Ventas, que permanece abierta de fondo.",price,true,false,target(price.txtCodigo,"Código + Enter"),target(price.labelPrecio,"Precio consultado"),target(price.btnCerrar,"Cerrar / Esc"));price.dispose();venta.dispose();
        });
        final HistorialVentasView[] hv=new HistorialVentasView[1];
        ui(()->{hv[0]=new HistorialVentasView();show(hv[0],1260,780);});waitFor(()->button(hv[0],"Buscar").isEnabled());
        ui(()->{
            JTable table=first(hv[0],JTable.class);if(table.getRowCount()!=1)throw new IllegalStateException("Historial de demo incorrecto");table.setRowSelectionInterval(0,0);
            scene("14-historial","Buscá ventas y reimprimí tickets","1 Elegí el período y tocá Buscar. 2 Seleccioná la venta. 3 Reimprimir ticket recupera su detalle guardado sin generar otro cobro. Anular exige motivo y confirmación.",hv[0],true,true,target(button(hv[0],"Buscar"),"Buscar por fechas y medio"),target(table,"Seleccionar venta"),target(button(hv[0],"Reimprimir"),"Reimprimir ticket"));
            button(hv[0],"Gráficos").doClick(0);
        });
        final GraficosVentasView[] gv=new GraficosVentasView[1];
        waitFor(()->{for(Window w:Window.getWindows())if(w instanceof GraficosVentasView && w.isVisible())gv[0]=(GraficosVentasView)w;return gv[0]!=null && button(gv[0],"Actualizar").isEnabled();});
        ui(()->{show(gv[0],1260,780);scene("15-graficos","Consultá cómo se vendió","1 Ajustá el período y Actualizar. Las tortas comparan importes por medio de pago y los cinco productos que más facturaron, agrupando el resto. No muestran unidades ni ganancias.",gv[0],true,false,target(button(gv[0],"Actualizar"),"Actualizar gráficos"));gv[0].dispose();
            scene("16-reportes","Guardá o imprimí el reporte","Elegí las fechas y el medio de pago en el historial. Tocá Reporte de ventas. En el visor, usá la impresora o guardá como PDF.",hv[0],true,false,target(button(hv[0],"Reporte"),"Reporte de ventas"));hv[0].dispose();
        });
        final AdministracionController[] admin=new AdministracionController[1];
        ui(()->{admin[0]=new AdministracionController();admin[0].loadAdminView();AdministracionView v=admin[0].getAdminView();show(v,1260,850);v.txtCodigo.setText("779000000004");v.txtNombre.setText("Azúcar 1 kg");v.txtPrecio.setText("1200");
            scene("17-productos","Cargá y modificá el catálogo","1 Completá código, nombre y precio. 2 Agregar producto registra uno nuevo. 3 Para modificar, seleccioná una fila, Editar seleccionado y Guardar cambios. Esta pantalla no administra stock.",v,true,false,target(v.txtCodigo,"Código, nombre y precio"),target(v.btnAgregar,"Agregar producto"),target(v.btnEditar,"Editar seleccionado"));v.dispose();
        });
        final ProductosSinCodigoView[] sv=new ProductosSinCodigoView[1];ui(()->{sv[0]=new ProductosSinCodigoView(null);show(sv[0],1120,730);});waitFor(()->button(sv[0],"Guardar").isEnabled());
        ui(()->{scene("18-opciones","Administrá los nombres sin código","1 Nuevo prepara una entrada. 2 Escribí el nombre y Guardar. Seleccioná una fila para renombrar o desactivar. El precio se ingresa al vender.",sv[0],true,false,target(button(sv[0],"Nuevo"),"Nuevo"),target(first(sv[0],JTextField.class),"Nombre"),target(button(sv[0],"Guardar"),"Guardar"));sv[0].dispose();
            caja=new CajaView();show(caja,1120,720);
        });waitFor(()->button(caja,"Cerrar caja").isEnabled());
        ui(()->{dialogMode="cierre";button(caja,"Cerrar caja").doClick(0);caja.dispose();
            EmpresaView business=new EmpresaView();show(business,1120,840);
            scene("20-empresa","Personalizá el negocio","Completá los datos del negocio. Elegir logo permite usar un PNG o JPG. Guardar aplica la identidad a pantallas y comprobantes. Los archivos quedan en la carpeta config de la instalación.",business,true,false,target(button(business,"Elegir logo"),"Elegir logo"),target(button(business,"Guardar"),"Guardar"));business.dispose();
        });
        Path demo=Paths.get("build/material-demo");Files.createDirectories(demo);
        BackupService backupService=new BackupService(demo);
        ui(()->{BackupView backup=new BackupView(backupService);show(backup,1120,840);List<JTextField> paths=new ArrayList<>();for(Component c:components((Container)content(backup)))if(c instanceof JTextField)paths.add((JTextField)c);paths.get(0).setText("D:\\Copias\\PuntoDeVenta");paths.get(1).setText("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe");
            scene("21-backup","Protegé tus datos","1 Elegí carpeta y programa mysqldump. 2 Guardá la configuración. 3 Hacer backup ahora crea una copia manual. Las automáticas requieren el sistema abierto y la PC encendida. Guardá también una copia fuera de esta PC. Las rutas mostradas son ejemplos.",backup,true,false,target(first(backup,JTextField.class),"Carpeta de copias"),target(button(backup,"Guardar configuración"),"Guardar configuración"),target(button(backup,"Hacer backup"),"Hacer backup ahora"));backup.dispose();menu.dispose();dialogs.stop();
        });backupService.close();
        System.out.println("Demo validada: una venta de $3.000, fondo $10.000, efectivo esperado $13.000. Base real intacta.");
    }
    static com.lowagie.text.Font pf(float n,int style,Color c){return new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,n,style,c);}
    static String esc(String text){return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
    static void pdfAndHtml()throws Exception{
        List<Scene> pages=new ArrayList<>();for(Scene s:scenes)if(s.manual)pages.add(s);
        Document doc=new Document(PageSize.A4.rotate(),32,32,30,32);PdfWriter writer=PdfWriter.getInstance(doc,Files.newOutputStream(OUT.resolve("Manual-ilustrado.pdf")));
        writer.setPageEvent(new PdfPageEventHelper(){public void onEndPage(PdfWriter w,Document d){
            ColumnText.showTextAligned(w.getDirectContent(),Element.ALIGN_LEFT,new Phrase("PUNTO DE VENTA · CAPTURAS CON DATOS DE EJEMPLO",pf(8,0,new Color(95,112,103))),32,17,0);
            ColumnText.showTextAligned(w.getDirectContent(),Element.ALIGN_RIGHT,new Phrase(String.format("%02d",w.getPageNumber()),pf(9,1,GREEN)),810,17,0);
        }});
        doc.addTitle("Punto de Venta - Manual ilustrado");doc.addAuthor("Punto de Venta");doc.open();
        Paragraph cover=new Paragraph("Manual ilustrado",pf(34,1,GREEN));cover.setSpacingBefore(12);cover.setSpacingAfter(7);doc.add(cover);
        doc.add(new Paragraph("Punto de Venta · Dónde tocar, paso a paso",pf(18,1,INK)));
        doc.add(new Paragraph("Capturas del sistema actual con productos y ventas de ejemplo. Los recuadros numerados indican los controles que se describen debajo de cada pantalla.",pf(12,0,INK)));
        com.lowagie.text.Image front=com.lowagie.text.Image.getInstance(scenes.get(0).image,null);front.scaleToFit(740,330);front.setSpacingBefore(14);doc.add(front);
        doc.add(new Paragraph("Incluye: caja, una venta completa, tickets, historial, gráficos, productos, configuración y backups. Para ampliar una captura, aumentá el zoom del PDF. Para imprimir, usá A4 horizontal y Ajustar al papel.",pf(11,0,INK)));
        StringBuilder html=new StringBuilder("<!doctype html><html lang='es'><meta charset='utf-8'><title>Manual ilustrado</title><style>body{margin:0;background:#edf3ef;color:#1f302a;font:16px/1.5 Arial}main{max-width:1440px;margin:auto;padding:28px}section{background:white;margin:24px 0;padding:28px;border-radius:12px}h1,h2{color:#167050}.capture{position:relative}.capture img{width:100%;display:block}.capture svg{position:absolute;inset:0;width:100%;height:100%}li{margin:8px 0}a{color:#167050}@media print{section{break-after:page}body{background:white}section{padding:0}main{padding:0}}@page{size:A4 landscape}</style><main><h1>Manual ilustrado · Punto de Venta</h1><p>Capturas del sistema con datos de ejemplo. Los números muestran dónde tocar. El video de venta se entrega por separado.</p><ol>");
        for(Scene s:pages)html.append("<li><a href='#").append(s.id).append("'>").append(esc(s.title)).append("</a></li>");html.append("</ol>");
        for(Scene s:pages){
            doc.newPage();doc.add(new Paragraph(s.title,pf(23,1,GREEN)));
            PdfContentByte cb=writer.getDirectContent();float scale=Math.min(778f/s.image.getWidth(),395f/s.image.getHeight());float iw=s.image.getWidth()*scale,ih=s.image.getHeight()*scale;float x=(842-iw)/2,y=132+(395-ih)/2;
            com.lowagie.text.Image im=com.lowagie.text.Image.getInstance(s.image,null);im.scaleAbsolute(iw,ih);im.setAbsolutePosition(x,y);doc.add(im);
            int i=0;for(Mark mark:s.marks){i++;Rectangle r=mark.r;float rx=x+r.x*scale,ry=y+ih-(r.y+r.height)*scale,rw=r.width*scale,rh=r.height*scale;
                cb.setColorStroke(RED);cb.setLineWidth(2);cb.roundRectangle(rx-3,ry-3,rw+6,rh+6,4);cb.stroke();float cx=Math.max(x+10,rx),cy=Math.min(y+ih-10,ry+rh+5);cb.setColorFill(RED);cb.circle(cx,cy,9);cb.fill();ColumnText.showTextAligned(cb,Element.ALIGN_CENTER,new Phrase(String.valueOf(i),pf(10,1,Color.WHITE)),cx,cy-3,0);
            }
            ColumnText col=new ColumnText(cb);col.setSimpleColumn(38,41,804,117);col.setLeading(15);col.addText(new Phrase(s.text,pf(12,0,INK)));
            if(!s.marks.isEmpty()){StringBuilder legend=new StringBuilder("\n");i=0;for(Mark m:s.marks)legend.append(++i).append(" ").append(m.text).append("   ");col.addText(new Phrase(legend.toString(),pf(10,1,GREEN)));}if(col.go()!=ColumnText.NO_MORE_TEXT)throw new IllegalStateException("Leyenda demasiado larga: "+s.id);
            html.append("<section id='").append(s.id).append("'><h2>").append(esc(s.title)).append("</h2><div class='capture'><img src='capturas/").append(s.id).append(".png' alt='").append(esc(s.title)).append("'><svg viewBox='0 0 ").append(s.image.getWidth()).append(' ').append(s.image.getHeight()).append("'>");i=0;
            for(Mark m:s.marks){i++;Rectangle r=m.r;int cx=Math.max(20,r.x),cy=Math.max(20,r.y);html.append("<rect x='").append(r.x-5).append("' y='").append(r.y-5).append("' width='").append(r.width+10).append("' height='").append(r.height+10).append("' rx='5' fill='none' stroke='#d0303c' stroke-width='4'/><circle cx='").append(cx).append("' cy='").append(cy).append("' r='18' fill='#d0303c'/><text x='").append(cx).append("' y='").append(cy+6).append("' text-anchor='middle' font-family='Arial' font-weight='bold' font-size='20' fill='white'>").append(i).append("</text>");}
            html.append("</svg></div><p>").append(esc(s.text)).append("</p><ol>");for(Mark m:s.marks)html.append("<li>").append(esc(m.text)).append("</li>");html.append("</ol></section>");
        }
        html.append("</main>");doc.close();Files.write(OUT.resolve("Manual-ilustrado.html"),html.toString().getBytes(StandardCharsets.UTF_8));
        PdfReader reader=new PdfReader(OUT.resolve("Manual-ilustrado.pdf").toString());if(reader.getNumberOfPages()!=pages.size()+1)throw new IllegalStateException("Número de páginas inesperado");reader.close();
        System.out.println("Manual ilustrado: "+(pages.size()+1)+" páginas verificadas.");
    }
    static void wrap(Graphics2D g,String text,int x,int y,int max,int leading){StringBuilder line=new StringBuilder();for(String word:text.split(" ")){String next=line.length()==0?word:line+" "+word;if(g.getFontMetrics().stringWidth(next)>max){g.drawString(line.toString(),x,y);y+=leading;line=new StringBuilder(word);}else line=new StringBuilder(next);}g.drawString(line.toString(),x,y);}
    static void video()throws Exception{
        List<Scene> video=new ArrayList<>();for(Scene s:scenes)if(s.video)video.add(s);
        AWTSequenceEncoder encoder=AWTSequenceEncoder.createSequenceEncoder(OUT.resolve("Venta-paso-a-paso.mp4").toFile(),4);int frame=0;
        for(int index=0;index<video.size();index++){
            Scene s=video.get(index);for(int f=0;f<24;f++){
                BufferedImage im=new BufferedImage(1440,1080,BufferedImage.TYPE_3BYTE_BGR);Graphics2D g=im.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setColor(new Color(237,244,239));g.fillRect(0,0,1440,1080);g.setColor(GREEN);g.fillRect(0,0,1440,120);g.setColor(Color.WHITE);g.setFont(new java.awt.Font("Segoe UI",java.awt.Font.BOLD,30));g.drawString("Cómo hacer una venta",36,44);g.setFont(new java.awt.Font("Segoe UI",java.awt.Font.PLAIN,19));g.drawString("PASO "+(index+1)+" / "+video.size()+" · "+s.title,36,86);g.setFont(new java.awt.Font("Segoe UI",java.awt.Font.BOLD,14));g.drawString("DEMO CON DATOS DE EJEMPLO",1110,43);
                double scale=Math.min(1368.0/s.image.getWidth(),790.0/s.image.getHeight());int width=(int)(s.image.getWidth()*scale),height=(int)(s.image.getHeight()*scale),x=(1440-width)/2,y=135+(790-height)/2;g.drawImage(s.image,x,y,width,height,null);
                int n=0;for(Mark m:s.marks){n++;Rectangle r=m.r;int rx=x+(int)(r.x*scale),ry=y+(int)(r.y*scale),rw=(int)(r.width*scale),rh=(int)(r.height*scale);g.setColor(RED);g.setStroke(new BasicStroke(3));g.drawRoundRect(rx-4,ry-4,rw+8,rh+8,6,6);g.fillOval(rx-14,ry-14,28,28);g.setColor(Color.WHITE);g.setFont(new java.awt.Font("Segoe UI",java.awt.Font.BOLD,17));g.drawString(String.valueOf(n),rx-5,ry+6);}
                if(!s.marks.isEmpty()){Rectangle r=s.marks.get((f/8)%s.marks.size()).r;int cx=x+(int)((r.x+r.width*.78)*scale),cy=y+(int)((r.y+r.height*.6)*scale);g.setColor(new Color(255,190,35,170));int rad=14+(f%8);g.fillOval(cx-rad,cy-rad,rad*2,rad*2);Polygon cursor=new Polygon(new int[]{cx,cx+5,cx+11,cx+17,cx+9,cx+9},new int[]{cy,cy+24,cy+19,cy+30,cy+33,cy+22},6);g.setColor(Color.WHITE);g.fill(cursor);g.setColor(INK);g.setStroke(new BasicStroke(2));g.draw(cursor);}
                g.setColor(Color.WHITE);g.fillRect(0,942,1440,138);g.setColor(INK);g.setFont(new java.awt.Font("Segoe UI",java.awt.Font.PLAIN,22));wrap(g,s.text,36,980,1368,30);g.setColor(new Color(208,225,215));g.fillRect(0,1072,1440,8);g.setColor(GREEN);g.fillRect(0,1072,(int)(1440.0*(frame+1)/(video.size()*24)),8);g.dispose();encoder.encodeImage(im);if(f==12)ImageIO.write(im,"png",OUT.resolve("capturas/video-"+s.id+".png").toFile());frame++;
            }System.out.println("Video: paso "+(index+1)+" de "+video.size());
        }encoder.finish();Files.write(OUT.resolve("LEEME.txt"),("MANUAL ILUSTRADO Y VIDEO DE VENTA\r\n\r\nManual-ilustrado.pdf: capturas del sistema con recuadros numerados.\r\nManual-ilustrado.html: versión para navegador; conservar junto a capturas.\r\nVenta-paso-a-paso.mp4: video guiado de "+(video.size()*6)+" segundos, con indicaciones en pantalla y sin audio. Puede pausarse en cada paso.\r\n\r\nLos productos y operaciones son de ejemplo. Se ejecutó una venta real de $3.000 en una base temporal en memoria, separada de la base del negocio.\r\nLa demo no envía nada a una impresora ni realiza pagos externos.\r\nEstos materiales se entregan por separado; no se agregaron al instalador.\r\n").getBytes(StandardCharsets.UTF_8));System.out.println("Video terminado: "+frame+" cuadros, "+(video.size()*6)+" segundos.");
    }
    public static void main(String[] args)throws Exception{
        java.util.Locale.setDefault(new java.util.Locale("es","AR"));
        Files.createDirectories(OUT.resolve("capturas"));
        int status=0;
        try{
            if(args.length>0 && args[0].equals("video")){
                try(java.io.ObjectInputStream in=new java.io.ObjectInputStream(Files.newInputStream(Paths.get("build/material-scenes.bin")))){scenes.addAll((List<Scene>)in.readObject());}
                for(Scene s:scenes)s.image=ImageIO.read(OUT.resolve("capturas/"+s.id+".png").toFile());
                video();
            }else{
                initDatabase();captureWorkflow();
                try(java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(Files.newOutputStream(Paths.get("build/material-scenes.bin")))){out.writeObject(scenes);}
                pdfAndHtml();if(args.length==0 || !args[0].equals("manual"))video();
            }
        }
        catch(Throwable error){status=1;error.printStackTrace();}
        finally{SwingUtilities.invokeAndWait(()->{if(dialogs!=null)dialogs.stop();for(Window w:Window.getWindows())w.dispose();});}
        System.exit(status);
    }
}
