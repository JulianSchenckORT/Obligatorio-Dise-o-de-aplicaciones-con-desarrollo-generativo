package ort.da.obligatoriodiseno.presentadores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ort.da.obligatoriodiseno.Dominio.Jugador;

@RestController
@RequestMapping("/login-jugador")
public class PresentadorLoginJugador extends PresentadorLogin<Jugador> {

    @Override
    protected String siguienteCU() {
        return "jugador/tablero-jugador.html";
    }

    @Override
    protected Jugador obtenerUsuario(String nombreUsuario, String contrasenia) {
        return fachada.loginJugador(nombreUsuario, contrasenia);
    }

    @Override
    protected String getLoginPage() {
        return "login.html";
    }

    @Override
    protected String getClaveSesion() {
        return CLAVE_JUGADOR;
    }
}
