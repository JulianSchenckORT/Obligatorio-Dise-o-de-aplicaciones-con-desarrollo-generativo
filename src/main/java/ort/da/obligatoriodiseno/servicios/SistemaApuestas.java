package ort.da.obligatoriodiseno.servicios;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.FormaDeApostar;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.RegistroParticipacion;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Simple;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Super;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Triple;
import ort.da.obligatoriodiseno.dtos.ApuestaEnCursoDto;
import ort.da.obligatoriodiseno.dtos.ApuestaJugadorDto;
import ort.da.obligatoriodiseno.eventos.EventoSistema;
import ort.da.obligatoriodiseno.eventos.PublicadorEventos;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;

public class SistemaApuestas {
    private final SistemaCarrera sistemaCarrera;

    public SistemaApuestas(SistemaCarrera sistemaCarrera) {
        this.sistemaCarrera = sistemaCarrera;
    }

    public Apuesta prepararApuesta(Jugador jugador, int nroCarrera, int nroCaballo, double monto, String tipoApuesta)
            throws ApuestaException {
        if (monto <= 0) {
            throw new ApuestaException("El monto de la apuesta debe ser mayor a cero");
        }
        Carrera carrera = sistemaCarrera.buscarCarreraDisponible(nroCarrera);
        RegistroParticipacion caballo = sistemaCarrera.buscarCaballo(carrera, nroCaballo);
        return jugador.apostar(monto, caballo, crearFormaDeApostar(tipoApuesta));
    }

    public ApuestaEnCursoDto obtenerApuestaEnCurso(Apuesta apuesta) {
        RegistroParticipacion caballo = apuesta.getNroRegistroCaballo();
        Carrera carrera = sistemaCarrera.obtenerCarreraPorRegistro(caballo);
        double dividendo = caballo.getDividendo() > 0 ? caballo.getDividendo() : 1;
        double montoPotencial = apuesta.calcularpago(dividendo, carrera.getTotalApostado());

        return new ApuestaEnCursoDto(carrera.getJornada().getFecha(), carrera.getNumero(), carrera.getNombre(),
                caballo.getId(), caballo.getCaballo().getNombre(), obtenerNombreForma(apuesta.getFormaDeApostar()),
                dividendo, apuesta.getMonto(), apuesta.getFormaDeApostar().calcularCosto(apuesta.getMonto()),
                montoPotencial);
    }

    public void confirmarApuesta(Jugador jugador, Apuesta apuesta, String contrasenia) throws ApuestaException {
        if (contrasenia == null || contrasenia.isBlank()) {
            throw new ApuestaException("Debe ingresar la contrasena para confirmar la apuesta");
        }
        if (!jugador.esPasswordDe(jugador.getUsername(), contrasenia)) {
            throw new ApuestaException("Contrasena incorrecta");
        }
        try {
            jugador.confirmarApuesta(apuesta);
        } catch (IllegalStateException e) {
            throw new ApuestaException(e.getMessage());
        }
        notificarTablerosActualizados();
    }

    public void descartarApuesta(Jugador jugador, Apuesta apuesta) {
        try {
            jugador.descartarApuesta(apuesta);
        } catch (IllegalStateException e) {
            throw new ApuestaException(e.getMessage());
        }
        notificarTablerosActualizados();
    }

    public List<Apuesta> obtenerApuestasOrdenadas(Jugador jugador) {
        List<Apuesta> ordenadas = new ArrayList<>(jugador.getHistorialApuestas());
        ordenadas.sort(Comparator
                .comparing((Apuesta apuesta) -> sistemaCarrera
                        .obtenerCarreraPorRegistro(apuesta.getNroRegistroCaballo()).getJornada().getFecha())
                .reversed()
                .thenComparing(apuesta -> sistemaCarrera
                        .obtenerCarreraPorRegistro(apuesta.getNroRegistroCaballo()).getNumero())
                .thenComparing(apuesta -> apuesta.getNroRegistroCaballo().getId()));
        return ordenadas;
    }

    ApuestaJugadorDto crearApuestaJugadorDto(Apuesta apuesta) {
        RegistroParticipacion caballo = apuesta.getNroRegistroCaballo();
        Carrera carrera = sistemaCarrera.obtenerCarreraPorRegistro(caballo);
        boolean finalizada = carrera.estaFinalizada();
        double dividendoFinal = finalizada ? caballo.getDividendo() : 0;
        double montoCobrado = finalizada ? apuesta.calcularpago(dividendoFinal, carrera.getTotalApostado()) : 0;

        return new ApuestaJugadorDto(carrera.getJornada().getFecha(), carrera.getNumero(), carrera.getNombre(),
                caballo.getId(), caballo.getCaballo().getNombre(), apuesta.getMonto(),
                obtenerNombreForma(apuesta.getFormaDeApostar()), dividendoFinal, montoCobrado,
                finalizada ? "FINALIZADA" : "EN CURSO");
    }

    private String obtenerNombreForma(FormaDeApostar forma) {
        if (forma instanceof Simple) {
            return "Simple";
        }
        if (forma instanceof Super) {
            return "Super";
        }
        if (forma instanceof Triple) {
            return "Multiple";
        }
        return forma.getClass().getSimpleName();
    }

    private FormaDeApostar crearFormaDeApostar(String tipoApuesta) {
        if (tipoApuesta == null || tipoApuesta.isBlank()) {
            throw new ApuestaException("Debe seleccionar un tipo de apuesta");
        }
        if ("Super".equalsIgnoreCase(tipoApuesta)) {
            return new Super();
        }
        if ("Multiple".equalsIgnoreCase(tipoApuesta) || "Triple".equalsIgnoreCase(tipoApuesta)) {
            return new Triple();
        }
        if ("Simple".equalsIgnoreCase(tipoApuesta)) {
            return new Simple();
        }
        throw new ApuestaException("Tipo de apuesta invalido");
    }

    private void notificarTablerosActualizados() {
        PublicadorEventos.getInstancia().notificar(new EventoSistema("TABLEROS_ACTUALIZADOS", null));
    }
}
