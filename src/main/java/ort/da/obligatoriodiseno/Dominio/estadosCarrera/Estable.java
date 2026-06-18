package ort.da.obligatoriodiseno.Dominio.estadosCarrera;

import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.EstadoCarrera;
import ort.da.obligatoriodiseno.Dominio.RegistroParticipacion;

public class Estable implements EstadoCarrera {

    @Override
    public void finalizar(Carrera carrera, RegistroParticipacion caballo) {
        throw new IllegalStateException("No se puede finalizar esta carrera");
    }

    @Override
    public void abrir(Carrera carrera) {
        throw new IllegalStateException("No se puede abrir esta carrera.");
    }

    @Override
    public void cerrar(Carrera carrera) {
        carrera.congelarDividendos();
        carrera.cambiarEstado(new Cerrada());
    }

    @Override
    public void verificarDividendos(Carrera carrera) {
        if (!carrera.todosDividendosValidos()) {
            carrera.cambiarEstado(new Abierta());
        }
    }
}
