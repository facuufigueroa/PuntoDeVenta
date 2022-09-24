package Controller;

import DataBase.Querys;
import View.AdministracionView;
import View.MenuPrincipalView;
import View.VentaView;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MainController implements ActionListener{
    
    private Querys query = new Querys();

    private MenuPrincipalView menuPrincipal = new MenuPrincipalView();
    
    private AdministracionView adminView = new AdministracionView();
    
    private VentaView ventaView = new VentaView();
    
    public Button btnAdministracion;
    
    public AdministracionController adminController = new AdministracionController();
    
    public VentaController ventaController = new VentaController();
    
    @Override
    public void actionPerformed(ActionEvent e) {
        accionBtnAdministracion(e);
        accionBtnPVenta(e);
    }

    public MainController() {
        this.menuPrincipal.btnAdministracion.addActionListener(this);
        this.menuPrincipal.btnPuntoDeVenta.addActionListener(this);
    }
    
    
    public void loadMenuPrincipal(){
        menuPrincipal.setVisible(true);
        menuPrincipal.setLocationRelativeTo(null);
      
    }
    
    public void accionBtnAdministracion(ActionEvent e){
        
        if(e.getSource() == menuPrincipal.btnAdministracion){
            
            adminController.loadAdminView();
        }
    }
    
    public void loadAdministracion(){
        getAdminView().setVisible(true);
        getAdminView().setLocationRelativeTo(null);
    }
    
    
   public void accionBtnPVenta(ActionEvent e){
       if(e.getSource() == menuPrincipal.btnPuntoDeVenta){
            loadPuntoDeVenta();
        }
   }
    
   public void loadPuntoDeVenta(){
       getVentaView().setVisible(true);
       getVentaView().setLocationRelativeTo(null);
   }    
    
    
    
    
    
    
    
    
    

    public AdministracionView getAdminView() {
        return adminView;
    }

    public void setAdminView(AdministracionView adminView) {
        this.adminView = adminView;
    }

    public Button getBtnAdministracion() {
        return btnAdministracion;
    }

    public void setBtnAdministracion(Button btnAdministracion) {
        this.btnAdministracion = btnAdministracion;
    }

    public MenuPrincipalView getMenuPrincipal() {
        return menuPrincipal;
    }

    public void setMenuPrincipal(MenuPrincipalView menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
    }

    public VentaView getVentaView() {
        return ventaView;
    }

    public void setVentaView(VentaView ventaView) {
        this.ventaView = ventaView;
    }
    
    
    
    public Querys getQuery() {
        return query;
    }

    public void setQuery(Querys query) {
        this.query = query;
    }

    
       
       
       
       
       
       
       
}
