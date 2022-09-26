package Model;


public class Producto {
    
    private String codigo;
    private String nombre;
    private int precio;
    private int id;

    public Producto(String codigo, String nombre, int precio,int id) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.id=id;
    }

    public Producto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
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
