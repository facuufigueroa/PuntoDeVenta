package Model;

import java.math.BigInteger;


public class Producto {
    
    private BigInteger codigo;
    private String nombre;
    private int precio;

    public Producto(BigInteger codigo, String nombre, int precio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
    }

    public BigInteger getCodigo() {
        return codigo;
    }

    public void setCodigo(BigInteger codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }
    
    
}
