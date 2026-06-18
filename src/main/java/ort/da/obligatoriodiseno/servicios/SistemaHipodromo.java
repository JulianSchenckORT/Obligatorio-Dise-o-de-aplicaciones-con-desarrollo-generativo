package ort.da.obligatoriodiseno.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Hipodromo;
import ort.da.obligatoriodiseno.Dominio.Jornada;
import ort.da.obligatoriodiseno.dtos.CarreraFinalizadaDto;
import ort.da.obligatoriodiseno.dtos.CarreraPendienteDto;
import ort.da.obligatoriodiseno.dtos.TableroAdministradorDto;
import ort.da.obligatoriodiseno.excepciones.HipodromoException;

public class SistemaHipodromo {
    private static final double PORCENTAJE_COMISION = 0.10;

    private final Hipodromo hipodromo;

    public SistemaHipodromo() {
        this.hipodromo = new Hipodromo(PORCENTAJE_COMISION);
    }

    public Jornada registrarJornada(LocalDate fecha) {
        if (fecha == null) {
            throw new HipodromoException("Debe indicar la fecha de la jornada");
        }
        if (obtenerJornada(fecha) != null) {
            throw new HipodromoException("Ya existe una jornada para la fecha indicada");
        }
        return new Jornada(fecha, hipodromo);
    }

    public Jornada obtenerJornada(LocalDate fecha) {
        for (Jornada jornada : hipodromo.getListaJornadas()) {
            if (jornada.getFecha().equals(fecha)) {
                return jornada;
            }
        }
        return null;
    }

    public Jornada obtenerJornadaActual() {
        Jornada actual = null;
        LocalDate hoy = LocalDate.now();
        for (Jornada jornada : hipodromo.getListaJornadas()) {
            if (!jornada.getFecha().isAfter(hoy)
                    && (actual == null || jornada.getFecha().isAfter(actual.getFecha()))) {
                actual = jornada;
            }
        }
        if (actual == null) {
            throw new HipodromoException("No hay jornadas definidas en el sistema");
        }
        return actual;
    }

    public Jornada obtenerJornadaAnterior(LocalDate fecha) {
        Jornada anterior = null;
        for (Jornada jornada : hipodromo.getListaJornadas()) {
            if (jornada.getFecha().isBefore(fecha)
                    && (anterior == null || jornada.getFecha().isAfter(anterior.getFecha()))) {
                anterior = jornada;
            }
        }
        if (anterior == null) {
            throw new HipodromoException("No hay una jornada anterior disponible");
        }
        return anterior;
    }

    public Jornada obtenerJornadaSiguiente(LocalDate fecha) {
        Jornada siguiente = null;
        for (Jornada jornada : hipodromo.getListaJornadas()) {
            if (jornada.getFecha().isAfter(fecha)
                    && (siguiente == null || jornada.getFecha().isBefore(siguiente.getFecha()))) {
                siguiente = jornada;
            }
        }
        if (siguiente == null) {
            throw new HipodromoException("No hay una jornada siguiente disponible");
        }
        return siguiente;
    }

    public List<Jornada> obtenerJornadasOrdenadas() {
        List<Jornada> jornadas = new ArrayList<>(hipodromo.getListaJornadas());
        jornadas.sort(Comparator.comparing(Jornada::getFecha));
        return jornadas;
    }

    public TableroAdministradorDto obtenerTableroAdministrador() {
        return armarTablero(obtenerJornadaActual());
    }

    public TableroAdministradorDto obtenerTableroAdministrador(LocalDate fecha) {
        Jornada jornada = obtenerJornada(fecha);
        if (jornada == null) {
            throw new HipodromoException("No existe una jornada para la fecha indicada");
        }
        return armarTablero(jornada);
    }

    public TableroAdministradorDto obtenerTableroJornadaAnterior(LocalDate fecha) {
        return armarTablero(obtenerJornadaAnterior(fecha));
    }

    public TableroAdministradorDto obtenerTableroJornadaSiguiente(LocalDate fecha) {
        return armarTablero(obtenerJornadaSiguiente(fecha));
    }

    private TableroAdministradorDto armarTablero(Jornada jornada) {
        TableroAdministradorDto dto = new TableroAdministradorDto();
        dto.setFechaJornada(jornada.getFecha());
        dto.setTotalApostado(jornada.GetTotalApostado());
        dto.setTotalPagado(jornada.GetTotalPagado());
        dto.setComisiones(hipodromo.getComisionByJornada(jornada));
        dto.setBalanceGeneral(jornada.CalcularBalance());
        dto.setCarrerasTotales(jornada.GetCarreras().size());

        for (Carrera carrera : jornada.getCarrerasOrdenadas()) {
            if (carrera.estaFinalizada()) {
                dto.setCarrerasFinalizadas(dto.getCarrerasFinalizadas() + 1);
                dto.getCarrerasFinalizadasDetalle().add(crearFinalizadaDto(carrera));
            } else {
                dto.setCarrerasFaltanCorrer(dto.getCarrerasFaltanCorrer() + 1);
                dto.getProximasCarreras().add(crearPendienteDto(carrera));
            }
        }
        dto.getCarrerasFinalizadasDetalle().sort(
                Comparator.comparingInt(CarreraFinalizadaDto::getNumero).reversed());
        return dto;
    }

    private CarreraFinalizadaDto crearFinalizadaDto(Carrera carrera) {
        String hora = carrera.getHoraFinal() == null ? "" : carrera.getHoraFinal().toString();
        String ganador = carrera.getGanador() == null ? "" : carrera.getGanador().getCaballo().getNombre();
        double dividendo = carrera.getGanador() == null ? 0 : carrera.getGanador().getDividendoFinal();
        return new CarreraFinalizadaDto(carrera.getNumero(), hora, carrera.getCaballos().size(),
                carrera.getTotalApostado(), carrera.getTotalPagado(), ganador, dividendo);
    }

    private CarreraPendienteDto crearPendienteDto(Carrera carrera) {
        return new CarreraPendienteDto(carrera.getNumero(), carrera.getNombreEstado(), carrera.getCaballos().size(),
                carrera.getTotalApostado(), carrera.getCantidadApuestas());
    }
}
