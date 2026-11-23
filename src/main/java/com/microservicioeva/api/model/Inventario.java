package com.microservicioeva.api.model;

public class Inventario {

    private int id_inventario;
    private int id_producto;
    private double cantidad;
    private int stock_minimo;
    private int stock_maximo;

    public Inventario(int stock_minimo, int stock_maximo, double cantidad, int id_producto, int id_inventario) {
        this.stock_minimo = stock_minimo;
        this.stock_maximo = stock_maximo;
        this.cantidad = cantidad;
        this.id_producto = id_producto;
        this.id_inventario = id_inventario;
    }

    public int getId_inventario() {
        return id_inventario;
    }

    public void setId_inventario(int id_inventario) {
        this.id_inventario = id_inventario;
    }

    public int getId_producto() {
        return id_producto;
    }

    public void setId_producto(int id_producto) {
        this.id_producto = id_producto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public int getStock_minimo() {
        return stock_minimo;
    }

    public void setStock_minimo(int stock_minimo) {
        this.stock_minimo = stock_minimo;
    }

    public int getStock_maximo() {
        return stock_maximo;
    }

    public void setStock_maximo(int stock_maximo) {
        this.stock_maximo = stock_maximo;
    }
}
