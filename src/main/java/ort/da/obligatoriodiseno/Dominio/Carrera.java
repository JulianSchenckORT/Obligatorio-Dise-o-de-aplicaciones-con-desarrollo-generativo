package ort.da.obligatoriodiseno.Dominio;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Definida;

@Getter
@Setter
public class Carrera {

    private Jornada jornada;
    private int numero;
    private String nombre;
    private List<RegistroParticipacion> caballos;
    private LocalTime horaFinal;
    private RegistroParticipacion ganador;
    private EstadoCarrera estado;

    public Carrera(int nroCarrera, String nombre, Jornada jornada) {
        this.numero = nroCarrera;
        this.nombre = nombre;
        this.jornada = jornada;
        this.caballos = new ArrayList<>();
        this.estado = new Definida();
        this.horaFinal = null;
        this.ganador = null;
    }

    public void finalizar(RegistroParticipacion caballo) {
        estado.finalizar(this, caballo);
    }

    public void abrir() {
        estado.abrir(this);
    }

    public double getTotalPagado() {
        return 0;
    }

    public double getTotalApostado() {
        double total = 0;
        for (RegistroParticipacion registro : this.caballos) {
            total += registro.getTotalApostado();
        }
        return total;
    }


    public double calcularDividendo(double comision, RegistroParticipacion caballo) {
        double totalCarrera = getTotalApostado();
        double totalRegistro = caballo.getTotalApostado();
        totalCarrera = totalCarrera * comision;

        if (totalRegistro == 0) {
            return 0;
        }
        return (totalCarrera - comision) / totalRegistro;
    }

    public void cambiarEstado(EstadoCarrera estado) {
        this.estado = estado;
    }

    public void setGanador(RegistroParticipacion ganador) {
        this.ganador = ganador;
    }

    public void recalcularDividendos() {
        for (RegistroParticipacion registro : this.caballos) {
            registro.calcularDividendo();
        }
    }
}
