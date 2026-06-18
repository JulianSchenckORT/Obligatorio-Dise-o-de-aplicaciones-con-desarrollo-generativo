package ort.da.obligatoriodiseno.Dominio.estadosApuesta;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.EstadoApuesta;
import ort.da.obligatoriodiseno.Dominio.Jugador;

public class Confirmada implements EstadoApuesta {

    @Override
    public void confirmar(Apuesta apuesta) {
        throw new IllegalStateException("La apuesta ya esta confirmada");
    }

    @Override
    public void descartar(Apuesta apuesta) {
        throw new IllegalStateException("La apuesta ya esta confirmada y no se puede descartar");
    }

    @Override
    public void pagar(double ganancias, Jugador jugador) {
        jugador.acreditarGanancia(ganancias);
    }
}
