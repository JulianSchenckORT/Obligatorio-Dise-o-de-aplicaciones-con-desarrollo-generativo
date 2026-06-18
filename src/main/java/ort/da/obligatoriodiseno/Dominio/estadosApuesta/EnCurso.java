package ort.da.obligatoriodiseno.Dominio.estadosApuesta;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.EstadoApuesta;
import ort.da.obligatoriodiseno.Dominio.Jugador;

public class EnCurso implements EstadoApuesta {

    public void descartar(Apuesta apuesta) {
        apuesta.cambiarEstado(new Descartada());
    }

    public void confirmar(Apuesta apuesta) {
        apuesta.cambiarEstado(new Confirmada());
        apuesta.getNroRegistroCaballo().agregarApuesta(apuesta);
    }

    @Override
    public void pagar(double ganancias, Jugador jugador) {
        throw new IllegalStateException("No se puede pagar una apuesta en curso");
    }
}
