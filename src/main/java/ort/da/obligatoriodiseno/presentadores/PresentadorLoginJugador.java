package ort.da.obligatoriodiseno.presentadores;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.servicios.fachada.Fachada;

@RestController
@RequestMapping("/login-jugador")
@Scope("session")
public class PresentadorLoginJugador extends PresentadorLogin<Jugador> {

    private final Fachada fachada = Fachada.getInstancia();

    @Override
    protected String siguienteCU() {
        return "jugador/tablero-jugador.html";
    }

    @Override
    protected Jugador obtenerUsuario(String nombreUsuario, String contrasenia) throws ApuestaException {
        return fachada.loginJugador(nombreUsuario, contrasenia);
    }

    @Override
    protected String getLoginPage() {
        return "login.html";
    }

    @Override
    protected String getClaveSesion() {
        return "Jugador";
    }
}
