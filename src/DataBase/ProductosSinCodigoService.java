package DataBase;

import java.sql.*;
import java.util.*;

/** Editable quick-sale names, independent of barcode prices and historical receipts. */
public final class ProductosSinCodigoService {
    interface Connections {Connection open() throws SQLException;}
    private final Connections connections;
    public ProductosSinCodigoService(){this(ConexionBD::getConnection);}
    ProductosSinCodigoService(Connections connections){this.connections=connections;}
    public static final class Entrada {
        public final long id;public final String nombre;public final boolean activo;
        Entrada(long id,String nombre,boolean activo){this.id=id;this.nombre=nombre;this.activo=activo;}
    }
    private Connection connect() throws SQLException {
        Connection c=connections.open();if(c==null)throw new SQLException("No se pudo conectar a la base de datos.");return c;
    }
    public List<Entrada> listar(boolean soloActivos) throws SQLException {
        List<Entrada> out=new ArrayList<>();
        try(Connection c=connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,nombre,activo FROM producto_sin_codigo"+(soloActivos ? " WHERE activo=TRUE" : "")+" ORDER BY nombre")) {
            while(r.next())out.add(new Entrada(r.getLong(1),r.getString(2),r.getBoolean(3)));
        }return out;
    }
    public void guardar(Long id,String nombre) throws SQLException {
        String normalizado=nombre==null ? "" : nombre.trim().replaceAll("\\s+"," ");
        if(normalizado.isEmpty() || normalizado.length()>255)throw new IllegalArgumentException("Indicá un nombre de entre 1 y 255 caracteres.");
        try(Connection c=connect()) {
            try(PreparedStatement p=c.prepareStatement("SELECT id FROM producto_sin_codigo WHERE UPPER(nombre)=UPPER(?)")) {
                p.setString(1,normalizado);try(ResultSet r=p.executeQuery()) {if(r.next() && (id==null || r.getLong(1)!=id))throw new IllegalArgumentException("Ese nombre ya existe. Si está inactivo, seleccioná la entrada y activala.");}
            }
            try(PreparedStatement p=c.prepareStatement(id==null ? "INSERT INTO producto_sin_codigo (nombre) VALUES (?)" : "UPDATE producto_sin_codigo SET nombre=? WHERE id=?")) {
                p.setString(1,normalizado);if(id!=null)p.setLong(2,id);
                if(p.executeUpdate()!=1)throw new SQLException("La entrada ya no existe. Actualizá la lista.");
            }
        }
    }
    public void activar(long id,boolean activo) throws SQLException {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("UPDATE producto_sin_codigo SET activo=? WHERE id=?")) {
            p.setBoolean(1,activo);p.setLong(2,id);if(p.executeUpdate()!=1)throw new SQLException("La entrada ya no existe. Actualizá la lista.");
        }
    }
}
