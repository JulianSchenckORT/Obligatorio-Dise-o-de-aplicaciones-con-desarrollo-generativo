package ort.da.obligatoriodiseno.Dominio;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Cerrada;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Definida;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Finalizada;

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
    }

    public synchronized void finalizar(RegistroParticipacion caballo) {
        estado.finalizar(this, caballo);
    }

    public synchronized void abrir() {
        estado.abrir(this);
    }

    public synchronized void cerrar() {
        estado.cerrar(this);
    }

    public double getTotalPagado() {
        if (!(estado instanceof Finalizada)) {
            return 0;
        }
        return caballos.stream().mapToDouble(RegistroParticipacion::getTotalPagado).sum();
    }

    public double getTotalApostado() {
        return caballos.stream().mapToDouble(RegistroParticipacion::getTotalApostado).sum();
    }

    public void pagar(RegistroParticipacion caballo) {
        caballo.pagarApuestas();
    }

    public double calcularDividendo(RegistroParticipacion caballo) {
        double totalCarrera = getTotalApostado();
        double totalRegistro = caballo.getTotalApostado();
        if (totalRegistro == 0) {
            return 0;
        }
        double porcentajeComision = jornada.getHipodromo() == null ? 0.10 : jornada.getHipodromo().getComision();
        return totalCarrera * (1 - porcentajeComision) / totalRegistro;
    }

    public void cambiarEstado(EstadoCarrera estado) {
        this.estado = estado;
    }

    public void setGanador(RegistroParticipacion ganador) {
        this.ganador = ganador;
        this.horaFinal = LocalTime.now();
    }

    public void agregarParticipante(Caballo caballo, int numero) {
        caballos.add(new RegistroParticipacion(caballo, numero, this));
    }

    public int getCantidadApuestas() {
        return caballos.stream().mapToInt(registro -> registro.getListaApuestas().size()).sum();
    }

    public boolean estaFinalizada() {
        return estado instanceof Finalizada;
    }

    public boolean estaCerrada() {
        return estado instanceof Cerrada;
    }

    public String getNombreEstado() {
        return estado.getClass().getSimpleName().toUpperCase();
    }

    public void recalcularDividendos() {
        for (RegistroParticipacion registro : caballos) {
            registro.calcularDividendo();
        }
        estado.verificarDividendos(this);
    }

    public boolean todosDividendosValidos() {
        return caballos.stream().allMatch(registro -> registro.getListaApuestas().size() > 0
                && registro.getDividendo() > 1);
    }

    public void congelarDividendos() {
        caballos.forEach(RegistroParticipacion::congelarDividendo);
    }
}
