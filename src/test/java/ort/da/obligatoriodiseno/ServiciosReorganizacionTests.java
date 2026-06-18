package ort.da.obligatoriodiseno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Caballo;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Cerrada;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Simple;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Super;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Triple;
import ort.da.obligatoriodiseno.dtos.TableroAdministradorDto;
import ort.da.obligatoriodiseno.dtos.TableroJugadorDto;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.servicios.SistemaApuestas;
import ort.da.obligatoriodiseno.servicios.SistemaCaballo;
import ort.da.obligatoriodiseno.servicios.SistemaCarrera;
import ort.da.obligatoriodiseno.servicios.SistemaHipodromo;
import ort.da.obligatoriodiseno.servicios.SistemaUsuarios;

class ServiciosReorganizacionTests {
    private static final double DELTA = 0.0001;

    private SistemaHipodromo sistemaHipodromo;
    private SistemaCaballo sistemaCaballo;
    private SistemaCarrera sistemaCarrera;
    private SistemaApuestas sistemaApuestas;
    private SistemaUsuarios sistemaUsuarios;
    private Carrera carrera;
    private Jugador jugador;

    @BeforeEach
    void configurarServicios() {
        sistemaHipodromo = new SistemaHipodromo();
        sistemaCaballo = new SistemaCaballo();
        sistemaCarrera = new SistemaCarrera(sistemaHipodromo, sistemaCaballo);
        sistemaApuestas = new SistemaApuestas(sistemaCarrera);
        sistemaApuestas.registrarModalidad(new Simple());
        sistemaApuestas.registrarModalidad(new Triple());
        sistemaApuestas.registrarModalidad(new Super());
        sistemaUsuarios = new SistemaUsuarios(sistemaCarrera, sistemaApuestas);

        sistemaHipodromo.registrarJornada(LocalDate.now());
        carrera = sistemaCarrera.agregarCarrera(LocalDate.now(), "Carrera de prueba");
        sistemaCarrera.agregarParticipante(new Caballo("Uno"), carrera);
        sistemaCarrera.agregarParticipante(new Caballo("Dos"), carrera);
        carrera.abrir();

        jugador = new Jugador("jugador", "clave", "Jugador de Prueba", 1000);
        sistemaUsuarios.registrarUsuario(jugador);
    }

    @Test
    void prepararYFallarLaContraseniaNoModificanElSaldo() {
        Apuesta apuesta = preparar(jugador, 1, 100, "Simple");

        assertEquals(1000, jugador.getSaldo(), DELTA);
        ApuestaException error = assertThrows(ApuestaException.class,
                () -> sistemaApuestas.confirmarApuesta(jugador, apuesta, "incorrecta"));
        assertEquals("Contraseña incorrecta", error.getMessage());
        assertEquals(1000, jugador.getSaldo(), DELTA);
        assertTrue(jugador.getHistorialApuestas().isEmpty());
    }

    @Test
    void rechazaMontosMenoresAUno() {
        ApuestaException error = assertThrows(ApuestaException.class,
                () -> preparar(jugador, 1, 0.5, "Simple"));

        assertEquals("Monto inválido", error.getMessage());
        assertEquals(1000, jugador.getSaldo(), DELTA);
    }

    @Test
    void validaElSaldoAlConfirmarSinDebitarAnteElError() {
        Jugador jugadorSinSaldo = new Jugador("sinSaldo", "clave", "Sin Saldo", 100);
        Apuesta apuesta = preparar(jugadorSinSaldo, 1, 100, "Súper");

        ApuestaException error = assertThrows(ApuestaException.class,
                () -> sistemaApuestas.confirmarApuesta(jugadorSinSaldo, apuesta, "clave"));

        assertEquals("Saldo insuficiente", error.getMessage());
        assertEquals(100, jugadorSinSaldo.getSaldo(), DELTA);
        assertTrue(jugadorSinSaldo.getHistorialApuestas().isEmpty());
    }

    @Test
    void confirmarDescuentaYRegistraComoUnaOperacion() {
        Apuesta apuesta = preparar(jugador, 1, 100, "Simple");
        sistemaApuestas.confirmarApuesta(jugador, apuesta, "clave");

        TableroJugadorDto tableroJugador = sistemaUsuarios.obtenerTableroJugador(jugador);
        TableroAdministradorDto tableroAdministrador = sistemaHipodromo.obtenerTableroAdministrador();
        assertEquals(900, tableroJugador.getSaldoActual(), DELTA);
        assertEquals(1, tableroJugador.getMisApuestas().size());
        assertEquals("Por correr", tableroJugador.getMisApuestas().getFirst().getEstado());
        assertEquals(100, tableroAdministrador.getTotalApostado(), DELTA);
    }

    @Test
    void noPermiteConfirmarDosVecesLaMismaApuesta() {
        Apuesta apuesta = preparar(jugador, 1, 100, "Simple");
        sistemaApuestas.confirmarApuesta(jugador, apuesta, "clave");

        assertThrows(ApuestaException.class,
                () -> sistemaApuestas.confirmarApuesta(jugador, apuesta, "clave"));
        assertEquals(900, jugador.getSaldo(), DELTA);
        assertEquals(1, jugador.getHistorialApuestas().size());
        assertEquals(1, carrera.getCantidadApuestas());
    }

    @Test
    void descartarApuestaNoNecesitaReintegrarSaldo() {
        Apuesta apuesta = preparar(jugador, 1, 100, "Simple");
        sistemaApuestas.descartarApuesta(jugador, apuesta);

        assertEquals(1000, jugador.getSaldo(), DELTA);
        assertTrue(jugador.getHistorialApuestas().isEmpty());
    }

    @Test
    void noConfirmaLuegoDeCerrarYConservaElDividendoFinal() {
        Jugador segundo = new Jugador("segundo", "clave", "Segundo", 1000);
        confirmar(jugador, 1, 100, "Simple");
        confirmar(segundo, 2, 100, "Simple");
        Apuesta pendiente = preparar(jugador, 1, 50, "Simple");
        double dividendoAlCerrar = carrera.getCaballos().getFirst().getDividendo();

        carrera.cerrar();
        ApuestaException error = assertThrows(ApuestaException.class,
                () -> sistemaApuestas.confirmarApuesta(jugador, pendiente, "clave"));

        assertEquals("Esta carrera ya no recibe apuestas", error.getMessage());
        assertEquals(dividendoAlCerrar, carrera.getCaballos().getFirst().getDividendoFinal(), DELTA);
        assertEquals(2, carrera.getCantidadApuestas());
        assertEquals(900, jugador.getSaldo(), DELTA);
    }

    @Test
    void pagaAlGanadorEnSaldoYNoInformaCobroAlPerdedor() {
        Jugador perdedor = new Jugador("perdedor", "clave", "Jugador Perdedor", 1000);
        confirmar(jugador, 1, 100, "Simple");
        confirmar(perdedor, 2, 100, "Simple");
        carrera.cerrar();
        carrera.finalizar(carrera.getCaballos().getFirst());

        TableroJugadorDto tableroGanador = sistemaUsuarios.obtenerTableroJugador(jugador);
        TableroJugadorDto tableroPerdedor = sistemaUsuarios.obtenerTableroJugador(perdedor);
        assertEquals(1080, jugador.getSaldo(), DELTA);
        assertEquals(180, jugador.getGanancias(), DELTA);
        assertEquals(180, tableroGanador.getMisApuestas().getFirst().getMontoCobrado(), DELTA);
        assertEquals(0, tableroPerdedor.getMisApuestas().getFirst().getMontoCobrado(), DELTA);
        assertEquals("Finalizada", tableroPerdedor.getMisApuestas().getFirst().getEstado());
    }

    @Test
    void tripleUsaElMontoNetoApostadoAlCaballo() {
        Jugador triple = new Jugador("triple", "clave", "Jugador Triple", 200000);
        Jugador simple = new Jugador("simple", "clave", "Jugador Simple", 200000);
        Apuesta apuestaTriple = confirmar(triple, 1, 99999, "Triple");
        confirmar(simple, 2, 100001, "Simple");

        assertEquals(200000, carrera.getTotalApostado(), DELTA);
        assertEquals(99999, carrera.getCaballos().getFirst().getTotalApostado(), DELTA);
        carrera.cerrar();
        double dividendoFinal = carrera.getCaballos().getFirst().getDividendoFinal();
        carrera.finalizar(carrera.getCaballos().getFirst());

        double pagoEsperado = 99999 * dividendoFinal * 2;
        assertEquals(pagoEsperado, apuestaTriple.getMontoCobrado(), DELTA);
        assertEquals(200000 - (99999 * 1.5) + pagoEsperado, triple.getSaldo(), DELTA);
    }

    @Test
    void modalidadesRegistradasAlimentanElTablero() {
        assertEquals(java.util.List.of("Simple", "Triple", "Súper"),
                sistemaUsuarios.obtenerTableroJugador(jugador).getModalidadesApuesta());
    }

    @Test
    void registrarUnaModalidadExistenteLaReemplazaSinDuplicarla() {
        sistemaApuestas.registrarModalidad(new Simple());

        assertEquals(java.util.List.of("Simple", "Triple", "Súper"),
                sistemaApuestas.obtenerNombresModalidades());
    }

    @Test
    void carrerasFinalizadasSeOrdenanPorNumeroDescendente() {
        carrera.cambiarEstado(new Cerrada());
        carrera.finalizar(carrera.getCaballos().getFirst());
        Carrera segunda = sistemaCarrera.agregarCarrera(LocalDate.now(), "Segunda");
        sistemaCarrera.agregarParticipante(new Caballo("Tres"), segunda);
        segunda.cambiarEstado(new Cerrada());
        segunda.finalizar(segunda.getCaballos().getFirst());

        TableroAdministradorDto tablero = sistemaHipodromo.obtenerTableroAdministrador();
        assertEquals(2, tablero.getCarrerasFinalizadasDetalle().get(0).getNumero());
        assertEquals(1, tablero.getCarrerasFinalizadasDetalle().get(1).getNumero());
    }

    @Test
    void sistemaCaballoReutilizaElCaballoRegistradoYEvitaDuplicarloEnUnaCarrera() {
        Caballo registrado = sistemaCaballo.registrarCaballo("Uno");

        assertSame(carrera.getCaballos().getFirst().getCaballo(), registrado);
        assertSame(registrado, sistemaCaballo.registrarCaballo("Uno"));
        assertThrows(ApuestaException.class,
                () -> sistemaCarrera.agregarParticipante(new Caballo("Uno"), carrera));
    }

    private Apuesta preparar(Jugador apostador, int caballo, double monto, String modalidad) {
        return sistemaApuestas.prepararApuesta(
                apostador, LocalDate.now(), carrera.getNumero(), caballo, monto, modalidad);
    }

    private Apuesta confirmar(Jugador apostador, int caballo, double monto, String modalidad) {
        Apuesta apuesta = preparar(apostador, caballo, monto, modalidad);
        sistemaApuestas.confirmarApuesta(apostador, apuesta, "clave");
        return apuesta;
    }
}
