package DataBase;

import Model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Querys extends ConexionBD{
    
    private ConexionBD conexion = new ConexionBD();
   
    
    public void agregarProducto(Producto producto){
        PreparedStatement ps = null;
        Connection conn = getConnection();
        String sql = "INSERT INTO producto (codigo,nombre,precio) VALUES(?,?,?) ";
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre().toUpperCase());
            ps.setInt(3, producto.getPrecio());
            ps.execute();
        } catch (SQLException e) {
            System.err.println(e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
    
    public void eliminarProducto(Producto producto){
        PreparedStatement ps = null;
        Connection conn = getConnection();

        String sql = "DELETE FROM producto WHERE codigo = ?";

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, producto.getCodigo());
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println(e);
       
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println(e);
            }

        }
    }
    
    public void buscarProducto(String codigo){
        
    }
    
    public void modificarProducto(Producto producto, String idproducto){
        PreparedStatement ps = null;
        Connection con = getConnection();

        String sql = "UPDATE producto SET nombre = ? , precio = ? WHERE idproducto = ?";

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, producto.getNombre().toUpperCase());
            ps.setInt(2,producto.getPrecio());
            ps.setInt(4, producto.getId());

            ps.executeUpdate();
      
        } catch (SQLException e) {
            System.err.println(e);
            
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
    
    public int traer_id_producto(String codigo){
        int id_producto=0;
        PreparedStatement ps = null;
        Connection conn = getConnection();
        try {
            String sql = "SELECT idproducto FROM producto WHERE codigo = '" + codigo + "'";
            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery(sql);
            if (rs.next()) {
                id_producto = rs.getInt(1);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return id_producto;
    }
    
}
