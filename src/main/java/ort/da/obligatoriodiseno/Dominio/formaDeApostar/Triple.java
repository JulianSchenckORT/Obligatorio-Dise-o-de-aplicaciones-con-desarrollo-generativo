package ort.da.obligatoriodiseno.Dominio.formaDeApostar;

import ort.da.obligatoriodiseno.Dominio.FormaDeApostar;

public class Triple extends FormaDeApostar {
    @Override
    public String getNombre() {
        return "Triple";
    }

    @Override
    public double calcularPago(double monto, double dividendo, double totalApostadoCaballo) {
        double multiplicador = totalApostadoCaballo < 100000 ? 2 : 3;
        return monto * dividendo * multiplicador;
    }

    @Override
    public double calcularCosto(double monto) {
        return monto * 1.5;
    }
}
