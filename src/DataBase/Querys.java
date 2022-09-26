package DataBase;

import Model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

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
    
    public Producto buscarPorCodigo(String codigo){
        String[] dato = new String[3];
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = getConnection();
        Producto producto = new Producto();

        String sql = "SELECT * FROM producto WHERE codigo = '" + codigo + "'";

        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                producto.setCodigo(rs.getString("codigo"));
                producto.setNombre(rs.getString("nombre"));
                producto.setPrecio(rs.getInt("precio"));
            }
        } catch (Exception e) {
            System.err.println(e);
            return null;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println(e);
            }
        }
        return producto;
    }
    
    public boolean verificarCodigoExistente(String codigo){
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = getConnection();
        String sql = "SELECT * FROM producto WHERE codigo = '" + codigo + "'";
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        }catch (Exception e) {
                System.err.println(e);
        } 
        return false;
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
    
    public void listarProductos(DefaultTableModel model){
       Connection conn = conexion.getConnection();
        Statement st;
        String[] dato = new String[3];

        try {
            String sql = "SELECT * FROM producto ORDER BY nombre ";

            st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                dato[0] = rs.getString(2);
                dato[1] = rs.getString(3);
                dato[2] = "$"+rs.getString(4);
                model.addRow(dato);

            }

        } catch (Exception e) {
            System.out.println(e);
        }

   }
   
    public boolean editar(Producto producto) {
        PreparedStatement ps = null;
        Connection con = getConnection();

        String sql = "UPDATE producto SET codigo = ?, nombre = ? , precio = ? WHERE idproducto = ?";

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre().toUpperCase());
            ps.setInt(3, producto.getPrecio());
            ps.setInt(4, producto.getId());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
   
    public boolean borrar(Producto producto) {
        PreparedStatement ps = null;
        Connection conn = getConnection();
        String sql = "DELETE FROM producto WHERE codigo = ?";

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, producto.getCodigo());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {

            System.err.println(e);
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println(e);
            }

        }
    }
}
