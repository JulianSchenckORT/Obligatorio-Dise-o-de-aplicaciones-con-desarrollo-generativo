package ort.da.obligatoriodiseno.presentadores;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.dtos.CarreraDto;
import ort.da.obligatoriodiseno.dtos.TableroAdministradorDto;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.servicios.fachada.Fachada;
import ort.da.obligatoriodiseno.utils.Command;
import ort.da.obligatoriodiseno.utils.Commands;

@RestController
@RequestMapping("/admin/apuestas")
public class PresentadorApuesta {

    private final Fachada fachada = Fachada.getInstancia();

    @GetMapping("/tablero")
    public Commands obtenerTablero(HttpSession sesionHttp) throws ApuestaException {
        Admin admin = validarAdministrador(sesionHttp);
        return Command.lista(new Command("mostrarTableroAdministrador",
                personalizar(fachada.obtenerTableroAdministrador(), admin)));
    }

    @GetMapping("/tablero/jornada")
    public Commands obtenerTableroPorJornada(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) throws ApuestaException {

        Admin admin = validarAdministrador(sesionHttp);
        return Command.lista(new Command("mostrarTableroAdministrador",
                personalizar(fachada.obtenerTableroAdministrador(fecha), admin)));
    }

    @GetMapping("/tablero/jornada/anterior")
    public Commands obtenerTableroJornadaAnterior(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) throws ApuestaException {

        Admin admin = validarAdministrador(sesionHttp);
        return Command.lista(new Command("mostrarTableroAdministrador",
                personalizar(fachada.obtenerTableroJornadaAnterior(fecha), admin)));
    }

    @GetMapping("/tablero/jornada/siguiente")
    public Commands obtenerTableroJornadaSiguiente(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) throws ApuestaException {

        Admin admin = validarAdministrador(sesionHttp);
        return Command.lista(new Command("mostrarTableroAdministrador",
                personalizar(fachada.obtenerTableroJornadaSiguiente(fecha), admin)));
    }

    @PostMapping("/carreras/{numero}/gestionar")
    public Commands gestionarCarrera(
            @PathVariable int numero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) throws ApuestaException {

        validarAdministrador(sesionHttp);
        CarreraDto carrera = fachada.obtenerCarreraParaGestion(fecha, numero);
        return Command.lista(new Command("gestionarCarrera", carrera));
    }

    @PostMapping("/carreras/{numero}/abrir")
    public Commands abrirCarrera(
            @PathVariable int numero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) throws ApuestaException {

        Admin admin = validarAdministrador(sesionHttp);
        CarreraDto carrera = fachada.abrirCarrera(fecha, numero);
        TableroAdministradorDto tablero = personalizar(fachada.obtenerTableroAdministrador(fecha), admin);
        return Command.lista(
                new Command("gestionarCarrera", carrera),
                new Command("mostrarTableroAdministrador", tablero),
                new Command("mensaje", "Carrera abierta"));
    }

    @PostMapping("/carreras/{numero}/cerrar")
    public Commands cerrarCarrera(
            @PathVariable int numero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) throws ApuestaException {

        Admin admin = validarAdministrador(sesionHttp);
        CarreraDto carrera = fachada.cerrarCarrera(fecha, numero);
        TableroAdministradorDto tablero = personalizar(fachada.obtenerTableroAdministrador(fecha), admin);
        return Command.lista(
                new Command("gestionarCarrera", carrera),
                new Command("mostrarTableroAdministrador", tablero),
                new Command("mensaje", "Carrera cerrada"));
    }

    @PostMapping("/carreras/{numero}/finalizar")
    public Commands finalizarCarrera(
            @PathVariable int numero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer caballoGanador,
            HttpSession sesionHttp) throws ApuestaException {

        Admin admin = validarAdministrador(sesionHttp);
        CarreraDto carrera = fachada.finalizarCarrera(fecha, numero, caballoGanador);
        TableroAdministradorDto tablero = personalizar(fachada.obtenerTableroAdministrador(fecha), admin);
        return Command.lista(
                new Command("gestionarCarrera", carrera),
                new Command("mostrarTableroAdministrador", tablero),
                new Command("mensaje", "Carrera finalizada"));
    }

    private Admin validarAdministrador(HttpSession sesionHttp) throws ApuestaException {
        Object usuario = sesionHttp.getAttribute("Administrador");
        if (usuario instanceof Admin admin) {
            return admin;
        }
        throw new ApuestaException("Debe iniciar sesion como administrador");
    }

    private TableroAdministradorDto personalizar(TableroAdministradorDto tablero, Admin admin) {
        tablero.setNombreAdministrador(admin.getNombre());
        return tablero;
    }
}
