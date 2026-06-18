package ort.da.obligatoriodiseno.Dominio;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroParticipacion {
    private double dividendo;
    private double dividendoFinal;
    private int Id;
    private List<Apuesta> listaApuestas;
    private Carrera carrera;
    private Caballo caballo;

    public RegistroParticipacion() {
        this.listaApuestas = new ArrayList<>();
    }

    public RegistroParticipacion(Caballo caballo, int id, Carrera carrera) {
        this();
        this.caballo = caballo;
        this.Id = id;
        this.carrera = carrera;
        this.dividendo = 0;
        this.dividendoFinal = 0;
    }

    public double calcularDividendo() {
        dividendo = carrera.calcularDividendo(this);
        return dividendo;
    }

    public double getTotalApostado() {
        return listaApuestas.stream().mapToDouble(Apuesta::getMonto).sum();
    }

    public void agregarApuesta(Apuesta apuesta) {
        listaApuestas.add(apuesta);
        carrera.recalcularDividendos();
    }

    public void congelarDividendo() {
        dividendoFinal = dividendo;
    }

    public void pagarApuestas() {
        double totalApostadoCaballo = getTotalApostado();
        for (Apuesta apuesta : listaApuestas) {
            double ganancias = apuesta.calcularPago(dividendoFinal, totalApostadoCaballo);
            apuesta.pagar(ganancias);
        }
    }

    public double getTotalPagado() {
        return listaApuestas.stream().mapToDouble(Apuesta::getMontoCobrado).sum();
    }
}
