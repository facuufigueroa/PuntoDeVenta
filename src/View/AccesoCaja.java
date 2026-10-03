package View;

import Caja.CajaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

/** Shared entry check, including the case where an open sales window crosses midnight. */
public final class AccesoCaja {
    private final CajaService service=new CajaService();
    private long aceptada;
    private LocalDate diaAceptado;
    private boolean consultando;
    private boolean abriendo;
    private Runnable pendiente;
    public void revisarInicio(JFrame owner) { consultar(owner,false,() -> {}); }
    public void prepararVenta(JFrame owner,Runnable continuar) { consultar(owner,true,continuar); }
    private void consultar(JFrame owner,boolean requiereCaja,Runnable continuar) {
        if(consultando || abriendo) {pendiente=() -> consultar(owner,requiereCaja,continuar);return;}
        consultando=true;
        new SwingWorker<CajaService.Estado,Void>() {
            protected CajaService.Estado doInBackground() throws Exception {return service.estado();}
            protected void done() {
                try {
                    CajaService.Estado estado=get();
                    if(estado.id==0) {
                        aceptada=0;diaAceptado=null;
                        if(requiereCaja)abrir(owner,continuar);
                    } else if(estado.pendiente(LocalDate.now()) && (aceptada!=estado.id || !LocalDate.now().equals(diaAceptado))) {
                        String fecha=estado.apertura.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                        int opcion=JOptionPane.showOptionDialog(owner,"Tenés la caja #"+estado.id+" abierta desde el "+fecha+".\nPodés cerrar el turno anterior y abrir uno nuevo, o continuar trabajando en esa caja.","Caja pendiente de cierre",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,new String[]{"Ir al cierre","Continuar turno","Cancelar"},"Ir al cierre");
                        if(opcion==0)NavegacionVentanas.abrir(owner,new CajaView(() -> {if(requiereCaja)prepararVenta(owner,continuar);}));
                        else if(opcion==1) {aceptada=estado.id;diaAceptado=LocalDate.now();continuar.run();}
                    } else continuar.run();
                } catch(Exception e) {
                    JOptionPane.showMessageDialog(owner,"No se pudo comprobar la caja. Revisá la conexión y volvé a intentar.\n"+(e.getCause()==null ? e.getMessage() : e.getCause().getMessage()));
                } finally {
                    consultando=false;
                    if(pendiente!=null && !abriendo) {Runnable siguiente=pendiente;pendiente=null;SwingUtilities.invokeLater(siguiente);}
                }
            }
        }.execute();
    }
    private void abrir(JFrame owner,Runnable continuar) {
        int opcion=JOptionPane.showOptionDialog(owner,"No hay una caja abierta. Abrí un turno para poder registrar las ventas.","Abrir caja para vender",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,new String[]{"Abrir caja","Cancelar"},"Abrir caja");
        if(opcion!=0)return;
        BigDecimal fondo;
        while(true) {
            String valor=JOptionPane.showInputDialog(owner,"Fondo inicial en efectivo (podés ingresar 0)");
            if(valor==null)return;
            try {fondo=CajaService.importe(valor);break;}
            catch(IllegalArgumentException e) {JOptionPane.showMessageDialog(owner,e.getMessage());}
        }
        final BigDecimal monto=fondo;
        abriendo=true;
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() throws Exception {service.abrir(monto);return null;}
            protected void done() {
                abriendo=false;
                try {get();prepararVenta(owner,continuar);}
                catch(Exception e) {pendiente=null;JOptionPane.showMessageDialog(owner,"No se pudo confirmar la apertura. Revisá Control de caja antes de reintentar.\n"+(e.getCause()==null ? e.getMessage() : e.getCause().getMessage()));}
            }
        }.execute();
    }
}
