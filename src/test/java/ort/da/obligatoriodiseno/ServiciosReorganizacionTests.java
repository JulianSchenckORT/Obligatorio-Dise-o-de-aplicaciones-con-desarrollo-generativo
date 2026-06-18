package ort.da.obligatoriodiseno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Caballo;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.dtos.TableroAdministradorDto;
import ort.da.obligatoriodiseno.dtos.TableroJugadorDto;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.servicios.SistemaApuestas;
import ort.da.obligatoriodiseno.servicios.SistemaCaballo;
import ort.da.obligatoriodiseno.servicios.SistemaCarrera;
import ort.da.obligatoriodiseno.servicios.SistemaHipodromo;
import ort.da.obligatoriodiseno.servicios.SistemaUsuarios;

class ServiciosReorganizacionTests {
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
    void apuestaConfirmadaApareceEnElTableroDelJugadorYDelHipodromo() {
        Apuesta apuesta = sistemaApuestas.prepararApuesta(jugador, 1, 1, 100, "Simple");
        sistemaApuestas.confirmarApuesta(jugador, apuesta, "clave");

        TableroJugadorDto tableroJugador = sistemaUsuarios.obtenerTableroJugador(jugador);
        TableroAdministradorDto tableroAdministrador = sistemaHipodromo.obtenerTableroAdministrador();

        assertEquals(900, tableroJugador.getSaldoActual());
        assertEquals(1, tableroJugador.getMisApuestas().size());
        assertEquals(1, tableroJugador.getCarrerasDisponibles().size());
        assertEquals(100, tableroAdministrador.getTotalApostado());
    }

    @Test
    void descartarApuestaDevuelveElSaldo() {
        Apuesta apuesta = sistemaApuestas.prepararApuesta(jugador, 1, 1, 100, "Simple");
        sistemaApuestas.descartarApuesta(jugador, apuesta);

        assertEquals(1000, jugador.getSaldo());
        assertEquals(0, jugador.getHistorialApuestas().size());
    }

    @Test
    void sistemaCaballoReutilizaElCaballoRegistradoYEvitaDuplicarloEnUnaCarrera() {
        Caballo registrado = sistemaCaballo.registrarCaballo("Uno");

        assertSame(carrera.getCaballos().getFirst().getCaballo(), registrado);
        assertEquals(2, sistemaCaballo.getAllCaballos().size());
        assertThrows(ApuestaException.class,
                () -> sistemaCarrera.agregarParticipante(new Caballo("Uno"), carrera));
    }
}
