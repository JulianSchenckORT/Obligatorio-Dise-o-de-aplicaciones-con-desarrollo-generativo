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
import ort.da.obligatoriodiseno.servicios.fachada.Fachada;
import ort.da.obligatoriodiseno.utils.Command;
import ort.da.obligatoriodiseno.utils.Commands;

@RestController
@RequestMapping("/admin/apuestas")
public class PresentadorPaginaAdmin {

    private final Fachada fachada = Fachada.getInstancia();

    @GetMapping("/tablero")
    public Commands obtenerTablero(HttpSession sesionHttp) {
        Admin admin = validarAdministrador(sesionHttp);
        return respuestaTablero(fachada.obtenerTableroAdministrador(), admin);
    }

    @GetMapping("/tablero/jornada")
    public Commands obtenerTableroPorJornada(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) {
        Admin admin = validarAdministrador(sesionHttp);
        return respuestaTablero(fachada.obtenerTableroAdministrador(fecha), admin);
    }

    @GetMapping("/tablero/jornada/anterior")
    public Commands obtenerTableroJornadaAnterior(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) {
        Admin admin = validarAdministrador(sesionHttp);
        return respuestaTablero(fachada.obtenerTableroJornadaAnterior(fecha), admin);
    }

    @GetMapping("/tablero/jornada/siguiente")
    public Commands obtenerTableroJornadaSiguiente(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) {
        Admin admin = validarAdministrador(sesionHttp);
        return respuestaTablero(fachada.obtenerTableroJornadaSiguiente(fecha), admin);
    }

    @PostMapping("/carreras/{numero}/gestionar")
    public Commands gestionarCarrera(
            @PathVariable int numero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) {
        validarAdministrador(sesionHttp);
        return Command.lista(new Command("gestionarCarrera",
                fachada.obtenerCarreraParaGestion(fecha, numero)));
    }

    @PostMapping("/carreras/{numero}/abrir")
    public Commands abrirCarrera(
            @PathVariable int numero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) {
        Admin admin = validarAdministrador(sesionHttp);
        CarreraDto carrera = fachada.abrirCarrera(fecha, numero);
        return respuestaGestion(carrera, fachada.obtenerTableroAdministrador(fecha), admin, "Carrera abierta");
    }

    @PostMapping("/carreras/{numero}/cerrar")
    public Commands cerrarCarrera(
            @PathVariable int numero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession sesionHttp) {
        Admin admin = validarAdministrador(sesionHttp);
        CarreraDto carrera = fachada.cerrarCarrera(fecha, numero);
        return respuestaGestion(carrera, fachada.obtenerTableroAdministrador(fecha), admin, "Carrera cerrada");
    }

    @PostMapping("/carreras/{numero}/finalizar")
    public Commands finalizarCarrera(
            @PathVariable int numero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer caballoGanador,
            HttpSession sesionHttp) {
        Admin admin = validarAdministrador(sesionHttp);
        CarreraDto carrera = fachada.finalizarCarrera(fecha, numero, caballoGanador);
        return respuestaGestion(carrera, fachada.obtenerTableroAdministrador(fecha), admin, "Carrera finalizada");
    }

    private Commands respuestaTablero(TableroAdministradorDto tablero, Admin admin) {
        return Command.lista(new Command("mostrarTableroAdministrador", personalizar(tablero, admin)));
    }

    private Commands respuestaGestion(CarreraDto carrera, TableroAdministradorDto tablero,
            Admin admin, String mensaje) {
        return Command.lista(
                new Command("gestionarCarrera", carrera),
                new Command("mostrarTableroAdministrador", personalizar(tablero, admin)),
                new Command("mensaje", mensaje));
    }

    private Admin validarAdministrador(HttpSession sesionHttp) {
        return PresentadorLogin.validarSesion(sesionHttp, PresentadorLogin.CLAVE_ADMINISTRADOR,
                Admin.class, "Debe iniciar sesión como administrador");
    }

    private TableroAdministradorDto personalizar(TableroAdministradorDto tablero, Admin admin) {
        tablero.setNombreAdministrador(admin.getNombre());
        return tablero;
    }
}
