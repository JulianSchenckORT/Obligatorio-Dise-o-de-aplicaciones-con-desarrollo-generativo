package ort.da.obligatoriodiseno.presentadores;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import ort.da.obligatoriodiseno.servicios.fachada.Fachada;
import ort.da.obligatoriodiseno.utils.Command;
import ort.da.obligatoriodiseno.utils.Commands;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.Dominio.Usuario;

public abstract class PresentadorLogin<U extends Usuario> {

    private final Fachada fachada = Fachada.getInstancia();

    @PostMapping("/login")
    public Commands login(HttpSession sesionHttp,
            @RequestParam String usuario,
            @RequestParam String contrasenia) {

        try {
            U usuarioLogueado = obtenerUsuario(usuario, contrasenia);

            sesionHttp.setAttribute(getClaveSesion(), usuarioLogueado);

            return Command.lista(new Command("loginExitoso", siguienteCU()));

        } catch (ApuestaException e) {

            if ("El administrador ya tiene una sesion activa".equals(e.getMessage())) {
                return Command.lista(new Command("mensaje", e.getMessage()));
            }

            return Command.lista(new Command("mensaje", "Acceso denegado"));
        }
    }

    @PostMapping("/logout")
    public Commands logout(HttpSession sesionHttp) {

        Usuario usuario = (Usuario) sesionHttp.getAttribute(getClaveSesion());

        if (usuario != null) {
            fachada.logout(usuario);
            sesionHttp.removeAttribute(getClaveSesion());
        }

        sesionHttp.invalidate();

        return Command.lista(new Command("usuarioNoAutenticado", getLoginPage()));
    }

    protected abstract String siguienteCU();

    protected abstract U obtenerUsuario(String nombreUsuario, String contrasenia) throws ApuestaException;

    protected abstract String getLoginPage();

    protected abstract String getClaveSesion();
}
