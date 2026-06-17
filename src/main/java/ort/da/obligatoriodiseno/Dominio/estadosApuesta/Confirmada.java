package ort.da.obligatoriodiseno.Dominio.estadosApuesta;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.EstadoApuesta;
public class Confirmada implements EstadoApuesta {

    @Override
    public void confirmar(Apuesta apuesta) {
        throw new IllegalStateException("La apuesta ya está confirmada");
    }

    @Override
    public void descartar(Apuesta apuesta) {
        throw new IllegalStateException("La apuesta ya está confirmada, no se puede descartar");
    }

    @Override
    public void Pagar(double ganancias,Jugador jugador) {
        jugador.setGanancias(jugador.getGanancias() + ganancias);
    }

}
