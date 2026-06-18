package ort.da.obligatoriodiseno.presentadores;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.servicios.fachada.Fachada;
import ort.da.obligatoriodiseno.utils.Command;
import ort.da.obligatoriodiseno.utils.Commands;

@RestController
@RequestMapping("/jugador/apuestas")
public class PresentadorTableroJugador {

    private final Fachada fachada = Fachada.getInstancia();

    @GetMapping("/tablero")
    public Commands obtenerTablero(HttpSession sesionHttp) {
        Jugador jugador = validarJugador(sesionHttp);
        return Command.lista(new Command("mostrarTableroUsuario", fachada.obtenerTableroJugador(jugador)));
    }

    @PostMapping("/preparar")
    public Commands prepararApuesta(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam int nroCarrera,
            @RequestParam int nroCaballo,
            @RequestParam double monto,
            @RequestParam String tipoApuesta,
            HttpSession sesionHttp) {

        Jugador jugador = validarJugador(sesionHttp);
        Apuesta apuesta = fachada.prepararApuesta(jugador, fecha, nroCarrera, nroCaballo, monto, tipoApuesta);
        sesionHttp.setAttribute("ApuestaEnCurso", apuesta);
        return Command.lista(new Command("apuestaPreparada", "jugador/confirmar-apuesta.html"));
    }

    @GetMapping("/confirmacion")
    public Commands obtenerConfirmacion(HttpSession sesionHttp) {
        validarJugador(sesionHttp);
        Apuesta apuesta = validarApuestaEnCurso(sesionHttp);
        return Command.lista(new Command("mostrarApuestaEnCurso", fachada.obtenerApuestaEnCurso(apuesta)));
    }

    @PostMapping("/confirmar")
    public Commands confirmarApuesta(@RequestParam String contrasenia, HttpSession sesionHttp) {
        Jugador jugador = validarJugador(sesionHttp);
        Apuesta apuesta = validarApuestaEnCurso(sesionHttp);
        fachada.confirmarApuesta(jugador, apuesta, contrasenia);
        sesionHttp.removeAttribute("ApuestaEnCurso");
        return Command.lista(new Command("apuestaConfirmada", "jugador/tablero-jugador.html"));
    }

    @PostMapping("/descartar")
    public Commands descartarApuesta(HttpSession sesionHttp) {
        Jugador jugador = validarJugador(sesionHttp);
        Apuesta apuesta = validarApuestaEnCurso(sesionHttp);
        fachada.descartarApuesta(jugador, apuesta);
        sesionHttp.removeAttribute("ApuestaEnCurso");
        return Command.lista(new Command("apuestaDescartada", "jugador/tablero-jugador.html"));
    }

    private Jugador validarJugador(HttpSession sesionHttp) {
        return PresentadorLogin.validarSesion(sesionHttp, PresentadorLogin.CLAVE_JUGADOR,
                Jugador.class, "Debe iniciar sesión como jugador");
    }

    private Apuesta validarApuestaEnCurso(HttpSession sesionHttp) {
        Object apuesta = sesionHttp.getAttribute("ApuestaEnCurso");
        if (apuesta instanceof Apuesta apuestaEnCurso) {
            return apuestaEnCurso;
        }
        throw new ApuestaException("No hay apuesta en curso");
    }

}
