package Models;

public class Producto {

    private int id;
    private int nroCupon;
    private String nroProductor;
    private String nombre;
    private double pesoBruto;
    private double tara;
    private double descuento;
    private double pesoNeto;
    private int remito;
    private String fecha;
    //----------------------------------
    // CONSTRUCTOR COMPLETO (desde BD)
    //----------------------------------
    public Producto(
            int id,
            int nroCupon,
            String nroProductor,
            String nombre,
            double pesoBruto,
            double tara,
            double descuento,
            String fecha) {

        this.id           = id;
        this.nroCupon     = nroCupon;
        this.nroProductor = nroProductor;
        this.nombre       = nombre;
        this.pesoBruto    = pesoBruto;
        this.tara         = tara;
        this.descuento    = descuento;
        this.fecha        = fecha;
        this.remito       = nroCupon;
        this.pesoNeto     = calcularPesoNeto();
    }

    //----------------------------------
    // CONSTRUCTOR SIN ID (nuevo registro)
    //----------------------------------
    public Producto(
            int nroCupon,
            String nroProductor,
            String nombre,
            double pesoBruto,
            double tara,
            double descuento) {

        this(
                0,
                nroCupon,
                nroProductor,
                nombre,
                pesoBruto,
                tara,
                descuento,
                java.time.LocalDate.now().toString() // fecha se pasa acá
        );
    }

    //----------------------------------
    // LÓGICA DE NEGOCIO
    //----------------------------------
    private double calcularPesoNeto() {
        double neto = pesoBruto - tara;
        neto = neto - (neto * descuento / 100);
        return Math.round(neto * 100.0) / 100.0;
    }

    //----------------------------------
    // GETTERS
    //----------------------------------
    public int getId()              { return id; }
    public int getNroCupon()        { return nroCupon; }
    public String getNroProductor() { return nroProductor; }
    public String getNombre()       { return nombre; }
    public double getPesoBruto()    { return pesoBruto; }
    public double getTara()         { return tara; }
    public double getDescuento()    { return descuento; }
    public double getPesoNeto()     { return pesoNeto; }
    public int getRemito()          { return remito; }
    public String getFecha() { return fecha; }

    //----------------------------------
    // SETTERS
    //----------------------------------
    public void setId(int id)                  { this.id = id; }
    public void setNroCupon(int nroCupon)      {
        this.nroCupon = nroCupon;
        this.remito   = nroCupon;
    }
    public void setNroProductor(String v)      { this.nroProductor = v; }
    public void setNombre(String nombre)       { this.nombre = nombre; }
    public void setPesoBruto(double pesoBruto) {
        this.pesoBruto = pesoBruto;
        this.pesoNeto  = calcularPesoNeto();
    }
    public void setTara(double tara)           {
        this.tara     = tara;
        this.pesoNeto = calcularPesoNeto();
    }
    public void setDescuento(double descuento) {
        this.descuento = descuento;
        this.pesoNeto  = calcularPesoNeto();
    }

    //----------------------------------
    // TO STRING
    //----------------------------------
    @Override
    public String toString() {
        return "Cupón: "     + nroCupon
                + " | Prod: "   + nroProductor
                + " | Nombre: " + nombre
                + " | Neto: "   + pesoNeto + " kg";
    }
}