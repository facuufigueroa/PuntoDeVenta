package puntodeventa;

import Controller.MainController;
public class bruyen {

    
    public static void main(String[] args) {
       javax.swing.SwingUtilities.invokeLater(() -> {
           MainController mainController = new MainController();
           mainController.loadMenuPrincipal();
       });
            
    }
    
}
