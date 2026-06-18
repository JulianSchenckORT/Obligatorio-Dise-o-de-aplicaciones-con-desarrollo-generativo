package ort.da.obligatoriodiseno.Dominio.formaDeApostar;

import ort.da.obligatoriodiseno.Dominio.FormaDeApostar;

public class Super extends FormaDeApostar {
    @Override
    public String getNombre() {
        return "Súper";
    }

    @Override
    public double calcularPago(double monto, double dividendo, double totalApostadoCaballo) {
        double multiplicador = dividendo >= 2 ? 3 : 4;
        return monto * dividendo * multiplicador;
    }

    @Override
    public double calcularCosto(double monto) {
        return monto * 2;
    }
}
