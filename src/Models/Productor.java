package Models;

public class Productor {

    private int    id;
    private String nombre;
    private String telefono;
    private String direccion;

    public Productor(int id, String nombre, String telefono, String direccion) {
        this.id        = id;
        this.nombre    = nombre;
        this.telefono  = telefono  != null ? telefono  : "";
        this.direccion = direccion != null ? direccion : "";
    }

    public int    getId()        { return id; }
    public String getNombre()    { return nombre; }
    public String getTelefono()  { return telefono; }
    public String getDireccion() { return direccion; }

    public void setNombre(String v)    { this.nombre    = v; }
    public void setTelefono(String v)  { this.telefono  = v; }
    public void setDireccion(String v) { this.direccion = v; }

    @Override public String toString() { return nombre; }
}
