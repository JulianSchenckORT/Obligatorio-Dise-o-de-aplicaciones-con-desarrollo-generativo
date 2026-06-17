package ort.da.obligatoriodiseno.Dominio;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Cerrada;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Finalizada;
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

    public void cerrar() {
        estado.cerrar(this);
    }

    public double getTotalPagado() {
        if (!(estado instanceof Finalizada)) {
            return 0;
        }
        return this.ganador.calcularPago();
    }

    public double getTotalApostado() {
        double total = 0;
        for (RegistroParticipacion registro : this.caballos) {
            total += registro.getTotalApostado();
        }
        return total;
    }
    public void pagar (RegistroParticipacion caballo){
        caballo.pagarApuestas();
    }
    public double calcularDividendo(double porcentajeDisponible, RegistroParticipacion caballo) {
        double totalCarrera = getTotalApostado();
        double totalRegistro = caballo.getTotalApostado();

        if (totalRegistro == 0) {
            return 0;
        }
        return (totalCarrera * porcentajeDisponible) / totalRegistro;
    }

    public void cambiarEstado(EstadoCarrera estado) {
        this.estado = estado;
    }

    public void setGanador(RegistroParticipacion ganador) {
        this.ganador = ganador;
        this.horaFinal = LocalTime.now();
    }

    public void agregarParticipante(Caballo caballo, int numero) {
        this.caballos.add(new RegistroParticipacion(caballo, numero, this));
    }

    public int getCantidadApuestas() {
        int total = 0;
        for (RegistroParticipacion registro : caballos) {
            if (registro.getListaApuestas() != null) {
                total += registro.getListaApuestas().size();
            }
        }
        return total;
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
        for (RegistroParticipacion registro : this.caballos) {
            registro.calcularDividendo();
        }
         this.estado.verificarDividendos(this);
    }
    public boolean todosDividendosValidos() {
    for (RegistroParticipacion registro : this.caballos) {
        if (registro.getDividendo() <= 1) {
            return false;
        }
    }

    return true;
}
}
