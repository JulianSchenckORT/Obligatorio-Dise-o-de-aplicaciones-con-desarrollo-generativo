package ort.da.obligatoriodiseno.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.FormaDeApostar;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.RegistroParticipacion;
import ort.da.obligatoriodiseno.dtos.ApuestaEnCursoDto;
import ort.da.obligatoriodiseno.dtos.ApuestaJugadorDto;
import ort.da.obligatoriodiseno.eventos.EventoSistema;
import ort.da.obligatoriodiseno.eventos.PublicadorEventos;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;

public class SistemaApuestas {
    private final SistemaCarrera sistemaCarrera;
    private final SistemaModalidadesApuesta sistemaModalidades;

    public SistemaApuestas(SistemaCarrera sistemaCarrera, SistemaModalidadesApuesta sistemaModalidades) {
        this.sistemaCarrera = sistemaCarrera;
        this.sistemaModalidades = sistemaModalidades;
    }

    public Apuesta prepararApuesta(Jugador jugador, LocalDate fecha, int nroCarrera, int nroCaballo,
            double monto, String tipoApuesta) throws ApuestaException {
        validarMonto(monto);
        Carrera carrera = sistemaCarrera.buscarCarreraDisponible(fecha, nroCarrera);
        RegistroParticipacion caballo = sistemaCarrera.buscarCaballo(carrera, nroCaballo);
        FormaDeApostar modalidad = sistemaModalidades.obtener(tipoApuesta);
        return jugador.prepararApuesta(monto, caballo, modalidad);
    }

    public ApuestaEnCursoDto obtenerApuestaEnCurso(Apuesta apuesta) {
        RegistroParticipacion caballo = apuesta.getNroRegistroCaballo();
        Carrera carrera = sistemaCarrera.obtenerCarreraPorRegistro(caballo);
        double dividendo = caballo.getDividendo();
        double totalApostadoCaballo = caballo.getTotalApostado() + apuesta.getMonto();
        double montoPotencial = dividendo > 1
                ? apuesta.calcularPago(dividendo, totalApostadoCaballo)
                : 0;

        return new ApuestaEnCursoDto(carrera.getJornada().getFecha(), carrera.getNumero(), carrera.getNombre(),
                caballo.getId(), caballo.getCaballo().getNombre(), apuesta.getFormaDeApostar().getNombre(),
                dividendo, apuesta.getMonto(), apuesta.calcularCosto(), montoPotencial);
    }

    public void confirmarApuesta(Jugador jugador, Apuesta apuesta, String contrasenia) throws ApuestaException {
        validarContrasenia(jugador, contrasenia);
        validarMonto(apuesta.getMonto());
        Carrera carrera = sistemaCarrera.obtenerCarreraPorRegistro(apuesta.getNroRegistroCaballo());

        synchronized (carrera) {
            if (!sistemaCarrera.estaDisponibleParaApostar(carrera)) {
                throw new ApuestaException("Esta carrera ya no recibe apuestas");
            }
            try {
                jugador.confirmarApuesta(apuesta);
            } catch (ApuestaException e) {
                throw e;
            } catch (IllegalStateException e) {
                throw new ApuestaException(e.getMessage());
            }
        }
        notificarTablerosActualizados();
    }

    public void descartarApuesta(Jugador jugador, Apuesta apuesta) {
        try {
            jugador.descartarApuesta(apuesta);
        } catch (ApuestaException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw new ApuestaException(e.getMessage());
        }
    }

    public List<Apuesta> obtenerApuestasOrdenadas(Jugador jugador) {
        List<Apuesta> ordenadas = new ArrayList<>(jugador.getHistorialApuestas());
        Comparator<Apuesta> porFecha = Comparator.comparing(apuesta -> sistemaCarrera
                .obtenerCarreraPorRegistro(apuesta.getNroRegistroCaballo()).getJornada().getFecha());
        Comparator<Apuesta> porCarrera = Comparator.comparingInt(apuesta -> sistemaCarrera
                .obtenerCarreraPorRegistro(apuesta.getNroRegistroCaballo()).getNumero());
        Comparator<Apuesta> porCaballo = Comparator.comparingInt(apuesta -> apuesta.getNroRegistroCaballo().getId());
        ordenadas.sort(porFecha.reversed().thenComparing(porCarrera.reversed()).thenComparing(porCaballo.reversed()));
        return ordenadas;
    }

    ApuestaJugadorDto crearApuestaJugadorDto(Apuesta apuesta) {
        RegistroParticipacion caballo = apuesta.getNroRegistroCaballo();
        Carrera carrera = sistemaCarrera.obtenerCarreraPorRegistro(caballo);
        boolean finalizada = carrera.estaFinalizada();
        double dividendoFinal = finalizada ? caballo.getDividendoFinal() : 0;

        return new ApuestaJugadorDto(carrera.getJornada().getFecha(), carrera.getNumero(), carrera.getNombre(),
                caballo.getId(), caballo.getCaballo().getNombre(), apuesta.getMonto(),
                apuesta.getFormaDeApostar().getNombre(), dividendoFinal, apuesta.getMontoCobrado(),
                finalizada ? "Finalizada" : "Por correr");
    }

    private void validarMonto(double monto) {
        if (!Double.isFinite(monto) || monto < 1) {
            throw new ApuestaException("Monto inválido");
        }
    }

    private void validarContrasenia(Jugador jugador, String contrasenia) {
        if (contrasenia == null || !jugador.esPasswordDe(jugador.getUsername(), contrasenia)) {
            throw new ApuestaException("Contraseña incorrecta");
        }
    }

    private void notificarTablerosActualizados() {
        PublicadorEventos.getInstancia().notificar(new EventoSistema("TABLEROS_ACTUALIZADOS", null));
    }
}
