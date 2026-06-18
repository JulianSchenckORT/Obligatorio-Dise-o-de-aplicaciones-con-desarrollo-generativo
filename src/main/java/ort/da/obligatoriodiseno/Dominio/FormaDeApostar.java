package ort.da.obligatoriodiseno.Dominio;

public abstract class FormaDeApostar {
    public abstract String getNombre();

    public abstract double calcularPago(double monto, double dividendo, double totalApostadoCaballo);

    public abstract double calcularCosto(double monto);
}
