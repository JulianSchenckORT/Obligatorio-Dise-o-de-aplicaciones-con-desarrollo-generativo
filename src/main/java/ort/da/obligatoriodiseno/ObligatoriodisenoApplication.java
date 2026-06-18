package ort.da.obligatoriodiseno;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Caballo;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.RegistroParticipacion;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Abierta;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Simple;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Super;
import ort.da.obligatoriodiseno.Dominio.formaDeApostar.Triple;
import ort.da.obligatoriodiseno.servicios.fachada.Fachada;

@SpringBootApplication
public class ObligatoriodisenoApplication {

    public static void main(String[] args) {
        precargarDatos(Fachada.getInstancia());
        SpringApplication.run(ObligatoriodisenoApplication.class, args);
    }

    static void precargarDatos(Fachada fachada) {
        fachada.registrarUsuario(new Admin("a1", "a1", "Usuario Administrador"));
        fachada.registrarUsuario(new Admin("a2", "a2", "Segundo Administrador"));
        fachada.registrarUsuario(new Jugador("j1", "j1", "Usuario Jugador", 2000));
        fachada.registrarUsuario(new Jugador("j2", "j2", "Segundo Jugador", 3000));

        Simple modalidadSimple = new Simple();
        fachada.registrarModalidad(modalidadSimple);
        fachada.registrarModalidad(new Triple());
        fachada.registrarModalidad(new Super());

        List<Caballo> caballos = List.of(
                fachada.registrarCaballo("Relampago Celeste"),
                fachada.registrarCaballo("Fuerza Nortena"),
                fachada.registrarCaballo("Sombra de Luna"),
                fachada.registrarCaballo("El Paisano"));

        LocalDate hoy = LocalDate.now();
        LocalDate fechaAnterior = hoy.minusWeeks(1);
        LocalDate fechaFutura = hoy.plusWeeks(1);
        fachada.registrarJornada(hoy);
        fachada.registrarJornada(fechaAnterior);
        fachada.registrarJornada(fechaFutura);

        agregarCarrera(fachada, hoy, "Premio Apertura", caballos);
        agregarCarrera(fachada, hoy, "Clasico MalaPata", caballos);
        agregarCarrera(fachada, hoy, "Copa Primavera", caballos);

        Carrera estable = agregarCarrera(fachada, hoy, "Premio Listo para Cerrar", caballos);
        estable.cambiarEstado(new Abierta());
        precargarApuestas(estable, 4, modalidadSimple);

        Carrera cerradaAnterior = agregarCarrera(
                fachada, fechaAnterior, "Clasico de la Semana Pasada", caballos);
        cerradaAnterior.cambiarEstado(new Abierta());
        precargarApuestas(cerradaAnterior, 12, modalidadSimple);
        cerradaAnterior.cerrar();

        Carrera granPremioAnterior = agregarCarrera(fachada, fechaAnterior, "Gran Premio Anterior", caballos);
        granPremioAnterior.cambiarEstado(new Abierta());
        precargarApuestas(granPremioAnterior, 12, modalidadSimple);
        granPremioAnterior.cerrar();

        agregarCarrera(fachada, fechaFutura, "Premio Futuro", caballos);
    }

    private static Carrera agregarCarrera(
            Fachada fachada, LocalDate fecha, String nombre, List<Caballo> caballos) {
        Carrera carrera = fachada.registrarCarrera(fecha, nombre);
        for (Caballo caballo : caballos) {
            fachada.agregarParticipante(caballo, carrera);
        }
        return carrera;
    }

    private static void precargarApuestas(Carrera carrera, int cantidadPorCaballo, Simple modalidadSimple) {
        int indice = 1;
        for (RegistroParticipacion caballo : carrera.getCaballos()) {
            for (int i = 0; i < cantidadPorCaballo; i++) {
                Jugador jugador = new Jugador(
                        "demo" + indice, "demo" + indice, "Jugador Demo " + indice, 50000);
                Apuesta apuesta = new Apuesta(800 + (i * 50), caballo, jugador, modalidadSimple);
                jugador.confirmarApuesta(apuesta);
                indice++;
            }
        }
    }
}
