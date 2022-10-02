package DataBase;
import java.sql.Connection;
import java.sql.DriverManager;


public class ConexionBD {
    
    public static final String URL = "jdbc:mysql://localhost:3306/bru-yen";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "1234";
    
    
    
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
