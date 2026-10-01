package DataBase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public class ConexionBD {
    
    private static final Properties CONFIG = loadConfig();
    public static final String URL = setting("BRUYEN_DB_URL", "url", "jdbc:mysql://localhost:3306/bru-yen");
    public static final String USERNAME = setting("BRUYEN_DB_USER", "user", "root");
    public static final String PASSWORD = setting("BRUYEN_DB_PASSWORD", "password", "");

    private static Properties loadConfig() {
        Properties properties = new Properties();
        Path file = Paths.get("config", "database.properties");
        if (Files.exists(file)) try (InputStream stream = Files.newInputStream(file)) {
            properties.load(stream);
        } catch (java.io.IOException error) {
            throw new IllegalStateException("No se pudo leer config/database.properties", error);
        }
        return properties;
    }

    private static String setting(String environment, String key, String fallback) {
        String value = System.getenv(environment);
        return value != null ? value : CONFIG.getProperty(key, fallback);
    }
    
    
    
    public static Connection getConnection(){
        Connection con = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            con =(Connection) DriverManager.getConnection(URL,USERNAME,PASSWORD);
        }catch(Exception e){
            System.out.println(e);
            
        }
        return con;
    }

}
