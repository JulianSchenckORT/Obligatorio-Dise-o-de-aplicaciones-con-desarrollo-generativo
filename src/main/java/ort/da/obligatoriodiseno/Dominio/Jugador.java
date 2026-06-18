package ort.da.obligatoriodiseno.Dominio;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.excepciones.UsuarioException;

@Getter
@Setter
public class Jugador extends Usuario {
    private double Saldo;
    private List<Apuesta> HistorialApuestas;
    private double Ganancias;

    public Jugador() {
        this.HistorialApuestas = new ArrayList<>();
    }

    public Jugador(String username, String password, String nombre, double saldo) {
        super(username, password, nombre);
        this.Saldo = saldo;
        this.HistorialApuestas = new ArrayList<>();
        this.Ganancias = 0;
    }

    public Apuesta prepararApuesta(
            double monto, RegistroParticipacion registroCaballo, FormaDeApostar formaDeApostar) {
        return new Apuesta(monto, registroCaballo, this, formaDeApostar);
    }

    public synchronized void confirmarApuesta(Apuesta apuesta) {
        if (apuesta.getJugador() != this) {
            throw new ApuestaException("La apuesta no pertenece al jugador");
        }
        double costo = apuesta.calcularCosto();
        if (Saldo < costo) {
            throw new UsuarioException("Saldo insuficiente");
        }
        apuesta.confirmar();
        Saldo -= costo;
        HistorialApuestas.add(apuesta);
    }

    public void descartarApuesta(Apuesta apuesta) {
        if (apuesta.getJugador() != this) {
            throw new ApuestaException("La apuesta no pertenece al jugador");
        }
        apuesta.descartar();
    }

    public synchronized void acreditarGanancia(double ganancias) {
        Ganancias += ganancias;
        Saldo += ganancias;
    }

    public double calcularTotalApostado() {
        double total = 0;
        for (Apuesta apuesta : HistorialApuestas) {
            total += apuesta.getMonto();
        }
        return total;
    }
}
