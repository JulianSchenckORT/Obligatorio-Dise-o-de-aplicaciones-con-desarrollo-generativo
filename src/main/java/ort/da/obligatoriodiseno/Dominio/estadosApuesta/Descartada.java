package ort.da.obligatoriodiseno.Dominio.estadosApuesta;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.EstadoApuesta;
import ort.da.obligatoriodiseno.Dominio.Jugador;

public class Descartada implements EstadoApuesta {

    @Override
    public void confirmar(Apuesta apuesta) {
        throw new IllegalStateException("No se puede confirmar una apuesta descartada");
    }

    @Override
    public void descartar(Apuesta apuesta) {
        throw new IllegalStateException("La apuesta ya esta descartada");
    }

    @Override
    public void pagar(double ganancias, Jugador jugador) {
        throw new IllegalStateException("No se puede pagar una apuesta descartada");
    }
}
