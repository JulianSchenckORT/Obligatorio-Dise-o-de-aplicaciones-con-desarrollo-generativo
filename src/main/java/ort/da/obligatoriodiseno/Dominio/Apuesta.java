package ort.da.obligatoriodiseno.Dominio;

import lombok.Getter;
import lombok.Setter;
import ort.da.obligatoriodiseno.Dominio.estadosApuesta.EnCurso;

@Getter
@Setter
public class Apuesta {
    private double monto;
    private RegistroParticipacion nroRegistroCaballo;
    private Jugador jugador;
    private EstadoApuesta estadoApuesta;
    private FormaDeApostar formaDeApostar;
    private double montoCobrado;
    private boolean pagada;

    public Apuesta(double monto, RegistroParticipacion nroRegistroCaballo, Jugador jugador,
            FormaDeApostar formaDeApostar) {
        this.monto = monto;
        this.nroRegistroCaballo = nroRegistroCaballo;
        this.jugador = jugador;
        this.formaDeApostar = formaDeApostar;
        this.estadoApuesta = new EnCurso();
        this.montoCobrado = 0;
        this.pagada = false;
    }

    public void cambiarEstado(EstadoApuesta estado) {
        this.estadoApuesta = estado;
    }

    public double calcularPago(double dividendo, double totalApostadoCaballo) {
        return formaDeApostar.calcularPago(monto, dividendo, totalApostadoCaballo);
    }

    public void pagar(double ganancias) {
        if (pagada) {
            throw new IllegalStateException("La apuesta ya fue pagada");
        }
        estadoApuesta.pagar(ganancias, jugador);
        montoCobrado = ganancias;
        pagada = true;
    }

    public void confirmar() {
        estadoApuesta.confirmar(this);
    }

    public void descartar() {
        estadoApuesta.descartar(this);
    }

    public double calcularCosto() {
        return formaDeApostar.calcularCosto(monto);
    }
}
