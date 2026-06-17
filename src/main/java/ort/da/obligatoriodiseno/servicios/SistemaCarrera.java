package ort.da.obligatoriodiseno.servicios;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Caballo;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Hipodromo;
import ort.da.obligatoriodiseno.Dominio.Jornada;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.RegistroParticipacion;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Abierta;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Cerrada;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Estable;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Simple;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Super;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Triple;
import ort.da.obligatoriodiseno.dtos.CaballoParticipanteDto;
import ort.da.obligatoriodiseno.dtos.ApuestaEnCursoDto;
import ort.da.obligatoriodiseno.dtos.ApuestaJugadorDto;
import ort.da.obligatoriodiseno.dtos.CarreraDto;
import ort.da.obligatoriodiseno.dtos.CarreraFinalizadaDto;
import ort.da.obligatoriodiseno.dtos.CarreraPendienteDto;
import ort.da.obligatoriodiseno.dtos.TableroAdministradorDto;
import ort.da.obligatoriodiseno.dtos.TableroJugadorDto;
import ort.da.obligatoriodiseno.eventos.EventoSistema;
import ort.da.obligatoriodiseno.eventos.PublicadorEventos;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;

public class SistemaCarrera {
    private static final double PORCENTAJE_COMISION = 0.10;

    private List<Jornada> jornadas;
    private Hipodromo hipodromo;

    public SistemaCarrera() {
        this.jornadas = new ArrayList<>();
        this.hipodromo = new Hipodromo(PORCENTAJE_COMISION);
        precargarJornadas();
    }

    public List<Carrera> GetCarrerasDisponibles() {
        List<Carrera> disponibles = new ArrayList<>();
        for (Jornada jornada : jornadasOrdenadas()) {
            for (Carrera carrera : carrerasOrdenadas(jornada)) {
                if ("ABIERTA".equals(carrera.getNombreEstado()) || "ESTABLE".equals(carrera.getNombreEstado())) {
                    disponibles.add(carrera);
                }
            }
        }
        return disponibles;
    }

    public Carrera getCarrera(Date fecha, int id) {
        return getCarrera(toLocalDate(fecha), id);
    }

    public Carrera getCarrera(LocalDate fecha, int id) {
        Jornada jornada = obtenerJornada(fecha);
        return jornada == null ? null : jornada.getCarrera(id);
    }
    // hay que conectarlo con la fachada y con sistema caballos para poder seleccionar un caballo
    public void agregarParticipante(Caballo caballo, Carrera carrera) {
        carrera.agregarParticipante(caballo, carrera.getCaballos().size() + 1);
    }

    public TableroAdministradorDto obtenerTableroAdministrador() throws ApuestaException {
        return armarTablero(obtenerJornadaActual());
    }

    public TableroAdministradorDto obtenerTableroAdministrador(LocalDate fecha) throws ApuestaException {
        Jornada jornada = obtenerJornada(fecha);
        if (jornada == null) {
            throw new ApuestaException("No existe una jornada para la fecha indicada");
        }
        return armarTablero(jornada);
    }

    public TableroAdministradorDto obtenerTableroJornadaAnterior(LocalDate fecha) throws ApuestaException {
        Jornada anterior = null;
        for (Jornada jornada : jornadas) {
            if (jornada.getFecha().isBefore(fecha)
                    && (anterior == null || jornada.getFecha().isAfter(anterior.getFecha()))) {
                anterior = jornada;
            }
        }
        if (anterior == null) {
            throw new ApuestaException("No hay una jornada anterior disponible");
        }
        return armarTablero(anterior);
    }

    public TableroAdministradorDto obtenerTableroJornadaSiguiente(LocalDate fecha) throws ApuestaException {
        Jornada siguiente = null;
        for (Jornada jornada : jornadas) {
            if (jornada.getFecha().isAfter(fecha)
                    && (siguiente == null || jornada.getFecha().isBefore(siguiente.getFecha()))) {
                siguiente = jornada;
            }
        }
        if (siguiente == null) {
            throw new ApuestaException("No hay una jornada siguiente disponible");
        }
        return armarTablero(siguiente);
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
    // a sistema usuario
    public TableroJugadorDto obtenerTableroJugador(Jugador jugador) {
        TableroJugadorDto tablero = new TableroJugadorDto();
        tablero.setNombreJugador(jugador.getNombre());
        tablero.setIniciales(obtenerIniciales(jugador.getNombre()));
        tablero.setSaldoActual(jugador.getSaldo());
        tablero.setTotalApostado(jugador.calcularTotalApostado());
        tablero.setTotalGanado(jugador.getGanancias());

        for (Carrera carrera : GetCarrerasDisponibles()) {
            tablero.getCarrerasDisponibles().add(crearCarreraDto(carrera));
        }
        for (Apuesta apuesta : apuestasOrdenadas(jugador.getHistorialApuestas())) {
            tablero.getMisApuestas().add(crearApuestaJugadorDto(apuesta));
        }
        return tablero;
    }
    // cosas de apuesta a sistema usuarios o podemos hacer un sistemaApuestas
    public Apuesta prepararApuesta(Jugador jugador, int nroCarrera, int nroCaballo, double monto, String tipoApuesta)
            throws ApuestaException {

        if (monto <= 0) {
            throw new ApuestaException("El monto de la apuesta debe ser mayor a cero");
        }

        Carrera carrera = buscarCarreraDisponible(nroCarrera);
        RegistroParticipacion caballo = buscarCaballo(carrera, nroCaballo);
        return jugador.apostar(monto, caballo, crearFormaDeApostar(tipoApuesta));
    }
// cosas de apuesta a sistema usuarios o podemos hacer un sistemaApuestas
    public ApuestaEnCursoDto obtenerApuestaEnCurso(Apuesta apuesta) {
        RegistroParticipacion caballo = apuesta.getNroRegistroCaballo();
        Carrera carrera = buscarCarreraPorRegistro(caballo);
        double dividendo = caballo.getDividendo() > 0 ? caballo.getDividendo() : 1;
        double montoPotencial = apuesta.calcularpago(dividendo, carrera.getTotalApostado());

        return new ApuestaEnCursoDto(carrera.getJornada().getFecha(), carrera.getNumero(), carrera.getNombre(),
                caballo.getId(), caballo.getCaballo().getNombre(), obtenerNombreForma(apuesta.getFormaDeApostar()),
                dividendo, apuesta.getMonto(), apuesta.getFormaDeApostar().calcularCosto(apuesta.getMonto()),
                montoPotencial);
    }
// cosas de apuesta a sistema usuarios o podemos hacer un sistemaApuestas
    public void confirmarApuesta(Jugador jugador, Apuesta apuesta, String contrasenia) throws ApuestaException {
        if (contrasenia == null || contrasenia.isBlank()) {
            throw new ApuestaException("Debe ingresar la contrasena para confirmar la apuesta");
        }
        if (!jugador.esPasswordDe(jugador.getUsername(), contrasenia)) {
            throw new ApuestaException("Contrasena incorrecta");
        }
        try {
            apuesta.confirmar();
            jugador.getHistorialApuestas().add(apuesta);
        } catch (IllegalStateException e) {
            throw new ApuestaException(e.getMessage());
        }
        notificarTablerosActualizados();
    }
// cosas de apuesta a sistema usuarios o podemos hacer un sistemaApuestas
    public void descartarApuesta(Jugador jugador, Apuesta apuesta) {
        try {
            jugador.descartarApuesta(apuesta);
        } catch (IllegalStateException e) {
            throw new ApuestaException(e.getMessage());
        }
        notificarTablerosActualizados();
    }

    private void notificarTablerosActualizados() {
        PublicadorEventos.getInstancia().notificar(new EventoSistema("TABLEROS_ACTUALIZADOS", null));
    }
    // precargas a ObligatoriodisenoApplication
    private void precargarJornadas() {
        LocalDate hoy = LocalDate.now();
        Jornada actual = new Jornada(hoy, hipodromo);
        Jornada anterior = new Jornada(hoy.minusWeeks(1), hipodromo);
        Jornada futura = new Jornada(hoy.plusWeeks(1), hipodromo);

        actual.GetCarreras().add(crearCarreraDefinida(1, "Premio Apertura", actual));
        actual.GetCarreras().add(crearCarreraDefinida(2, "Clasico MalaPata", actual));
        actual.GetCarreras().add(crearCarreraDefinida(3, "Copa Primavera", actual));
        actual.GetCarreras().add(crearCarreraEstable(4, "Premio Listo para Cerrar", actual));

        anterior.GetCarreras().add(crearCarreraCerrada(1, "Clasico de la Semana Pasada", anterior));
        anterior.GetCarreras().add(crearCarreraCerrada(2, "Gran Premio Anterior", anterior));

        futura.GetCarreras().add(crearCarreraDefinida(1, "Premio Futuro", futura));

        jornadas.add(actual);
        jornadas.add(anterior);
        jornadas.add(futura);
    }
     // precargas a ObligatoriodisenoApplication
    private Carrera crearCarreraDefinida(int numero, String nombre, Jornada jornada) {
        Carrera carrera = new Carrera(numero, nombre, jornada);
        carrera.agregarParticipante(new Caballo("Relampago Celeste"), 1);
        carrera.agregarParticipante(new Caballo("Fuerza Nortena"), 2);
        carrera.agregarParticipante(new Caballo("Sombra de Luna"), 3);
        carrera.agregarParticipante(new Caballo("El Paisano"), 4);
        return carrera;
    }
 // precargas a ObligatoriodisenoApplication
    private Carrera crearCarreraEstable(int numero, String nombre, Jornada jornada) {
        Carrera carrera = crearCarreraDefinida(numero, nombre, jornada);
        carrera.cambiarEstado(new Abierta());
        precargarApuestas(carrera, 4);
        carrera.cambiarEstado(new Estable());
        return carrera;
    }
 // precargas a ObligatoriodisenoApplication
    private Carrera crearCarreraCerrada(int numero, String nombre, Jornada jornada) {
        Carrera carrera = crearCarreraDefinida(numero, nombre, jornada);
        carrera.cambiarEstado(new Abierta());
        precargarApuestas(carrera, 12);
        carrera.cambiarEstado(new Cerrada());
        return carrera;
    }
    // precargas al ObligatoriodisenioApp.java
    private void precargarApuestas(Carrera carrera, int cantidadPorCaballo) {
        int indice = 1;
        for (RegistroParticipacion caballo : carrera.getCaballos()) {
            for (int i = 0; i < cantidadPorCaballo; i++) {
                Jugador jugador = new Jugador("demo" + indice, "demo" + indice, "Jugador Demo " + indice, 50000);
                Apuesta apuesta = new Apuesta(800 + (i * 50), caballo, jugador, new Simple());
                apuesta.confirmar();
                indice++;
            }
        }
    }

    private TableroAdministradorDto armarTablero(Jornada jornada) {
        TableroAdministradorDto dto = new TableroAdministradorDto();
        dto.setFechaJornada(jornada.getFecha());
        dto.setTotalApostado(jornada.GetTotalApostado());
        dto.setTotalPagado(jornada.GetTotalPagado());
        dto.setComisiones(jornada.GetTotalApostado() * jornada.getHipodromo().getComision());
        dto.setBalanceGeneral(jornada.GetTotalApostado() - jornada.GetTotalPagado());
        dto.setCarrerasTotales(jornada.GetCarreras().size());

        for (Carrera carrera : carrerasOrdenadas(jornada)) {
            if (carrera.estaFinalizada()) {
                dto.setCarrerasFinalizadas(dto.getCarrerasFinalizadas() + 1);
                dto.getCarrerasFinalizadasDetalle().add(crearFinalizadaDto(carrera));
            } else {
                dto.setCarrerasFaltanCorrer(dto.getCarrerasFaltanCorrer() + 1);
                dto.getProximasCarreras().add(crearPendienteDto(carrera));
            }
        }
        dto.getCarrerasFinalizadasDetalle().sort(Comparator
                .comparing(CarreraFinalizadaDto::getHoraFin, Comparator.nullsLast(String::compareTo))
                .thenComparing(CarreraFinalizadaDto::getNumero));
        dto.getProximasCarreras().sort(Comparator.comparingInt(CarreraPendienteDto::getNumero));
        return dto;
    }

    private CarreraFinalizadaDto crearFinalizadaDto(Carrera carrera) {
        String hora = carrera.getHoraFinal() == null ? "" : carrera.getHoraFinal().toString();
        String ganador = carrera.getGanador() == null ? "" : carrera.getGanador().getCaballo().getNombre();
        double dividendo = carrera.getGanador() == null ? 0 : carrera.getGanador().getDividendo();
        return new CarreraFinalizadaDto(carrera.getNumero(), hora, carrera.getCaballos().size(),
                carrera.getTotalApostado(), carrera.getTotalPagado(), ganador, dividendo);
    }

    private CarreraPendienteDto crearPendienteDto(Carrera carrera) {
        return new CarreraPendienteDto(carrera.getNumero(), carrera.getNombreEstado(), carrera.getCaballos().size(),
                carrera.getTotalApostado(), carrera.getCantidadApuestas());
    }

    private CarreraDto crearCarreraDto(Carrera carrera) {
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
// cosas de apuesta a sistema usuarios o podemos hacer un sistemaApuestas
    private ApuestaJugadorDto crearApuestaJugadorDto(Apuesta apuesta) {
        RegistroParticipacion caballo = apuesta.getNroRegistroCaballo();
        Carrera carrera = buscarCarreraPorRegistro(caballo);
        boolean finalizada = carrera.estaFinalizada();
        double dividendoFinal = finalizada ? caballo.getDividendo() : 0;
        double montoCobrado = finalizada ? apuesta.calcularpago(dividendoFinal, carrera.getTotalApostado()) : 0;

        return new ApuestaJugadorDto(carrera.getJornada().getFecha(), carrera.getNumero(), carrera.getNombre(),
                caballo.getId(), caballo.getCaballo().getNombre(), apuesta.getMonto(),
                obtenerNombreForma(apuesta.getFormaDeApostar()), dividendoFinal, montoCobrado,
                finalizada ? "FINALIZADA" : "EN CURSO");
    }
    // entiendo que esto y el panel de admin seria mejor manejarlo desde sistema hipodromo
    private List<Jornada> jornadasOrdenadas() {
        List<Jornada> ordenadas = new ArrayList<>(jornadas);
        ordenadas.sort(Comparator.comparing(Jornada::getFecha));
        return ordenadas;
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

    private List<Apuesta> apuestasOrdenadas(List<Apuesta> apuestas) {
        List<Apuesta> ordenadas = new ArrayList<>(apuestas);
        ordenadas.sort(Comparator
                .comparing((Apuesta apuesta) -> buscarCarreraPorRegistro(apuesta.getNroRegistroCaballo()).getJornada().getFecha())
                .reversed()
                .thenComparing(apuesta -> buscarCarreraPorRegistro(apuesta.getNroRegistroCaballo()).getNumero())
                .thenComparing(apuesta -> apuesta.getNroRegistroCaballo().getId()));
        return ordenadas;
    }

    private Carrera buscarCarreraDisponible(int nroCarrera) throws ApuestaException {
        for (Carrera carrera : GetCarrerasDisponibles()) {
            if (carrera.getNumero() == nroCarrera) {
                return carrera;
            }
        }
        throw new ApuestaException("La carrera seleccionada no esta disponible para apostar");
    }
    // por que queremos buscar una carrera por su caballo??
    private Carrera buscarCarreraPorRegistro(RegistroParticipacion registro) {
        for (Jornada jornada : jornadas) {
            for (Carrera carrera : jornada.GetCarreras()) {
                if (carrera.getCaballos().contains(registro)) {
                    return carrera;
                }
            }
        }
        throw new ApuestaException("No se encontro la carrera asociada a la apuesta");
    }
    /// a utils
    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "--";
        }
        String[] partes = nombre.trim().split("\\s+");
        String primera = partes[0].substring(0, 1);
        String segunda = partes.length > 1 ? partes[1].substring(0, 1) : "";
        return (primera + segunda).toUpperCase();
    }
    //// cosas de apuesta a sistema usuarios o podemos hacer un sistemaApuestas
    private String obtenerNombreForma(ort.da.obligatoriodiseno.Dominio.FormaDeApostar forma) {
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
// cosas de apuesta a sistema usuarios o podemos hacer un sistemaApuestas
    private ort.da.obligatoriodiseno.Dominio.FormaDeApostar crearFormaDeApostar(String tipoApuesta) {
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
    // esto tendria que ir en sistema hipodromo
    private Jornada obtenerJornadaActual() throws ApuestaException {
        Jornada actual = null;
        LocalDate hoy = LocalDate.now();
        for (Jornada jornada : jornadas) {
            if (!jornada.getFecha().isAfter(hoy)
                    && (actual == null || jornada.getFecha().isAfter(actual.getFecha()))) {
                actual = jornada;
            }
        }
        if (actual == null) {
            throw new ApuestaException("No hay jornadas disponibles para mostrar");
        }
        return actual;
    }
    //parece mejor en sistema hipodromo
    private Jornada obtenerJornada(LocalDate fecha) {
        for (Jornada jornada : jornadas) {
            if (jornada.getFecha().equals(fecha)) {
                return jornada;
            }
        }
        return null;
    }

    private Carrera obtenerCarreraDominio(LocalDate fecha, int numero) throws ApuestaException {
        Carrera carrera = getCarrera(fecha, numero);
        if (carrera == null) {
            throw new ApuestaException("No existe la carrera seleccionada");
        }
        return carrera;
    }

    private RegistroParticipacion buscarCaballo(Carrera carrera, int numeroCaballo) throws ApuestaException {
        for (RegistroParticipacion caballo : carrera.getCaballos()) {
            if (caballo.getId() == numeroCaballo) {
                return caballo;
            }
        }
        throw new ApuestaException("No existe el caballo seleccionado para esa carrera");
    }
    // a utils
    private LocalDate toLocalDate(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
