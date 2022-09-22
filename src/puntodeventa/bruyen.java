package puntodeventa;

import DataBase.ConexionBD;
import View.MenuPrincipalView;


public class bruyen {

    
    public static void main(String[] args) {
       MenuPrincipalView menu = new MenuPrincipalView();
       menu.setVisible(true);
       ConexionBD conex = new ConexionBD();
       ConexionBD.getConnection();
    }
    
}
