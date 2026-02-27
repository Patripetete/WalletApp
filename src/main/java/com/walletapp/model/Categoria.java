package com.walletapp.model;

public class Categoria {
    private int id;
    private String nombre;
    private TipoTransaccion tipo;
    private Integer idUsuario;

    public Categoria() {}

    public Categoria(int id, String nombre, TipoTransaccion tipo, Integer idUsuario) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.idUsuario = idUsuario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    @Override
    public String toString() { return nombre; }
}
