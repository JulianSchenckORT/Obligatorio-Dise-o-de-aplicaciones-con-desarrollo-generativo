package ort.da.obligatoriodiseno.Dominio.estadosApuesta;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.EstadoApuesta;
import ort.da.obligatoriodiseno.Dominio.Jugador;
public class Descartada implements EstadoApuesta {

    @Override
    public void confirmar(Apuesta apuesta) {
        // TODO Auto-generated method stub
        throw new IllegalStateException("no se puede confirmar una apuesta descartada");
    }

    @Override
    public void descartar(Apuesta apuesta) {
        
        throw new IllegalStateException("La apuesta ya está descartada");
    }

    @Override
    public void Pagar(double ganancias,Jugador jugador) {
        // TODO Auto-generated method stub
        throw new IllegalStateException("no se puede pagar una apuesta descartada");
    }
}
