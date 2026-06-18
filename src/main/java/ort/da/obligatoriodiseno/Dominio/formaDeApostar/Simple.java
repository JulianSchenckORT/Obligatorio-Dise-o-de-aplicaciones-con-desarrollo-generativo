package ort.da.obligatoriodiseno.Dominio.formaDeApostar;

import ort.da.obligatoriodiseno.Dominio.FormaDeApostar;

public class Simple extends FormaDeApostar {
    @Override
    public String getNombre() {
        return "Simple";
    }

    @Override
    public double calcularPago(double monto, double dividendo, double totalApostadoCaballo) {
        return monto * dividendo;
    }

    @Override
    public double calcularCosto(double monto) {
        return monto;
    }
}
