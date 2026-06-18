package ort.da.obligatoriodiseno.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Caballo;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Jornada;
import ort.da.obligatoriodiseno.Dominio.RegistroParticipacion;
import ort.da.obligatoriodiseno.dtos.CaballoParticipanteDto;
import ort.da.obligatoriodiseno.dtos.CarreraDto;
import ort.da.obligatoriodiseno.eventos.EventoSistema;
import ort.da.obligatoriodiseno.eventos.PublicadorEventos;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.utils.FechaUtils;

public class SistemaCarrera {
    private final SistemaHipodromo sistemaHipodromo;
    private final SistemaCaballo sistemaCaballo;

    public SistemaCarrera(SistemaHipodromo sistemaHipodromo, SistemaCaballo sistemaCaballo) {
        this.sistemaHipodromo = sistemaHipodromo;
        this.sistemaCaballo = sistemaCaballo;
    }

    public List<Carrera> getCarrerasDisponibles() {
        List<Carrera> disponibles = new ArrayList<>();
        for (Jornada jornada : sistemaHipodromo.obtenerJornadasOrdenadas()) {
            for (Carrera carrera : carrerasOrdenadas(jornada)) {
                if (estaDisponibleParaApostar(carrera)) {
                    disponibles.add(carrera);
                }
            }
        }
        return disponibles;
    }

    public Carrera getCarrera(Date fecha, int id) {
        return getCarrera(FechaUtils.toLocalDate(fecha), id);
    }

    public Carrera getCarrera(LocalDate fecha, int id) {
        Jornada jornada = sistemaHipodromo.obtenerJornada(fecha);
        return jornada == null ? null : jornada.getCarrera(id);
    }

    public Carrera agregarCarrera(LocalDate fecha, String nombre) {
        Jornada jornada = sistemaHipodromo.obtenerJornada(fecha);
        if (jornada == null) {
            throw new ApuestaException("No existe una jornada para la fecha indicada");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ApuestaException("El nombre de la carrera es obligatorio");
        }
        return jornada.agregarCarrera(nombre.trim());
    }

    public void agregarParticipante(Caballo caballo, Carrera carrera) {
        if (carrera == null) {
            throw new ApuestaException("Debe indicar una carrera");
        }
        Caballo caballoRegistrado = sistemaCaballo.registrarCaballo(caballo);
        boolean yaParticipa = carrera.getCaballos().stream()
                .anyMatch(registro -> registro.getCaballo() == caballoRegistrado);
        if (yaParticipa) {
            throw new ApuestaException("El caballo ya participa en la carrera");
        }
        carrera.agregarParticipante(caballoRegistrado, carrera.getCaballos().size() + 1);
    }

    public CarreraDto obtenerCarreraParaGestion(LocalDate fecha, int numero) throws ApuestaException {
        return crearCarreraDto(obtenerCarreraDominio(fecha, numero));
    }

    public CarreraDto abrirCarrera(LocalDate fecha, int numero) throws ApuestaException {
        Carrera carrera = obtenerCarreraDominio(fecha, numero);
        try {
            carrera.abrir();
        } catch (IllegalStateException e) {
            throw new ApuestaException(e.getMessage());
        }
        notificarTablerosActualizados();
        return crearCarreraDto(carrera);
    }

    public CarreraDto cerrarCarrera(LocalDate fecha, int numero) throws ApuestaException {
        Carrera carrera = obtenerCarreraDominio(fecha, numero);
        try {
            carrera.cerrar();
        } catch (IllegalStateException e) {
            throw new ApuestaException(e.getMessage());
        }
        notificarTablerosActualizados();
        return crearCarreraDto(carrera);
    }

    public CarreraDto finalizarCarrera(LocalDate fecha, int numero, Integer caballoGanador) throws ApuestaException {
        if (caballoGanador == null) {
            throw new ApuestaException("Debe indicar el caballo ganador de la carrera");
        }
        Carrera carrera = obtenerCarreraDominio(fecha, numero);
        RegistroParticipacion ganador = buscarCaballo(carrera, caballoGanador);
        try {
            carrera.finalizar(ganador);
        } catch (IllegalStateException e) {
            throw new ApuestaException(e.getMessage());
        }
        notificarTablerosActualizados();
        return crearCarreraDto(carrera);
    }

    Carrera buscarCarreraDisponible(LocalDate fecha, int nroCarrera) throws ApuestaException {
        Carrera carrera = getCarrera(fecha, nroCarrera);
        if (carrera != null && estaDisponibleParaApostar(carrera)) {
            return carrera;
        }
        throw new ApuestaException("Esta carrera ya no recibe apuestas");
    }

    boolean estaDisponibleParaApostar(Carrera carrera) {
        return carrera != null && ("ABIERTA".equals(carrera.getNombreEstado())
                || "ESTABLE".equals(carrera.getNombreEstado()));
    }

    RegistroParticipacion buscarCaballo(Carrera carrera, int numeroCaballo) throws ApuestaException {
        for (RegistroParticipacion caballo : carrera.getCaballos()) {
            if (caballo.getId() == numeroCaballo) {
                return caballo;
            }
        }
        throw new ApuestaException("No existe el caballo seleccionado para esa carrera");
    }

    Carrera obtenerCarreraPorRegistro(RegistroParticipacion registro) {
        if (registro == null || registro.getCarrera() == null) {
            throw new ApuestaException("No se encontro la carrera asociada a la apuesta");
        }
        return registro.getCarrera();
    }

    CarreraDto crearCarreraDto(Carrera carrera) {
        return new CarreraDto(carrera.getNumero(), carrera.getNombre(), carrera.getJornada().getFecha(),
                carrera.getHoraFinal(), carrera.getNombreEstado(), carrera.getCaballos().size(),
                carrera.getTotalApostado(), carrera.getCantidadApuestas(), crearCaballosDto(carrera));
    }

    private List<CaballoParticipanteDto> crearCaballosDto(Carrera carrera) {
        List<CaballoParticipanteDto> caballos = new ArrayList<>();
        for (RegistroParticipacion caballo : caballosOrdenados(carrera)) {
            double total = caballo.getListaApuestas().stream().mapToDouble(Apuesta::getMonto).sum();
            caballos.add(new CaballoParticipanteDto(caballo.getId(), caballo.getCaballo().getNombre(),
                    caballo.getDividendo(), total, caballo.getListaApuestas().size()));
        }
        return caballos;
    }

    private Carrera obtenerCarreraDominio(LocalDate fecha, int numero) throws ApuestaException {
        Carrera carrera = getCarrera(fecha, numero);
        if (carrera == null) {
            throw new ApuestaException("No existe la carrera seleccionada");
        }
        return carrera;
    }

    private List<Carrera> carrerasOrdenadas(Jornada jornada) {
        List<Carrera> ordenadas = new ArrayList<>(jornada.GetCarreras());
        ordenadas.sort(Comparator.comparingInt(Carrera::getNumero));
        return ordenadas;
    }

    private List<RegistroParticipacion> caballosOrdenados(Carrera carrera) {
        List<RegistroParticipacion> ordenados = new ArrayList<>(carrera.getCaballos());
        ordenados.sort(Comparator.comparingInt(RegistroParticipacion::getId));
        return ordenados;
    }

    private void notificarTablerosActualizados() {
        PublicadorEventos.getInstancia().notificar(new EventoSistema("TABLEROS_ACTUALIZADOS", null));
    }
}
