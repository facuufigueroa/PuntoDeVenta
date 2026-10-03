package puntodeventa;

public final class Main {
    private Main(){}
    public static void main(String[] args){javax.swing.SwingUtilities.invokeLater(() -> {
        Runnable iniciar=() -> new Controller.MainController().loadMenuPrincipal();
        if(!java.nio.file.Files.exists(java.nio.file.Paths.get("config/database.properties")) && System.getenv("PDV_DB_URL")==null && System.getenv("BRUYEN_DB_URL")==null)new View.ConexionView(iniciar).setVisible(true);
        else iniciar.run();
    });}
}
