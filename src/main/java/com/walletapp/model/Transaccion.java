package com.walletapp.model;

public class Transaccion {
    private int id;
    private String descripcion;
    private double importe;
    private String fecha;
    private TipoTransaccion tipo;
    private int idCuenta;
    private int idCategoria;
    private String nombreCuenta;
    private String nombreCategoria;

    public Transaccion() {}

    public Transaccion(int id, String descripcion, double importe, String fecha,
                       TipoTransaccion tipo, int idCuenta, int idCategoria) {
        this.id = id;
        this.descripcion = descripcion;
        this.importe = importe;
        this.fecha = fecha;
        this.tipo = tipo;
        this.idCuenta = idCuenta;
        this.idCategoria = idCategoria;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }
    public int getIdCuenta() { return idCuenta; }
    public void setIdCuenta(int idCuenta) { this.idCuenta = idCuenta; }
    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
    public String getNombreCuenta() { return nombreCuenta; }
    public void setNombreCuenta(String nombreCuenta) { this.nombreCuenta = nombreCuenta; }
    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }
}
