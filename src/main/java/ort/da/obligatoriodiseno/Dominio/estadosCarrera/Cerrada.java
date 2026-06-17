package ort.da.obligatoriodiseno.Dominio.estadosCarrera;

import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.EstadoCarrera;
import ort.da.obligatoriodiseno.Dominio.RegistroParticipacion;

public class Cerrada implements EstadoCarrera {

    @Override
    public void finalizar(Carrera carrera, RegistroParticipacion caballo) {
        if (caballo == null) {
            throw new IllegalStateException("Debe indicar el caballo ganador de la carrera");
        }
        carrera.setGanador(caballo);
        carrera.cambiarEstado(new Finalizada());
        carrera.pagar(caballo);
    }

    @Override
    public void abrir(Carrera carrera) {
        throw new IllegalStateException("No se puede abrir una carrera cerrada.");
    }

    @Override
    public void cerrar(Carrera carrera) {
        throw new IllegalStateException("La carrera ya está cerrada.");
    }
    @Override
    public void verificarDividendos(Carrera carrera) {
        ;
    }
}
