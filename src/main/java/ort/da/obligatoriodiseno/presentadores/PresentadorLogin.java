package ort.da.obligatoriodiseno.presentadores;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import ort.da.obligatoriodiseno.servicios.fachada.Fachada;
import ort.da.obligatoriodiseno.utils.Command;
import ort.da.obligatoriodiseno.utils.Commands;
import ort.da.obligatoriodiseno.excepciones.UsuarioException;
import ort.da.obligatoriodiseno.Dominio.Usuario;

public abstract class PresentadorLogin<U extends Usuario> {

    static final String CLAVE_JUGADOR = "Jugador";
    static final String CLAVE_ADMINISTRADOR = "Administrador";

    protected final Fachada fachada = Fachada.getInstancia();

    @PostMapping("/login")
    public Commands login(HttpSession sesionHttp,
            @RequestParam String usuario,
            @RequestParam String contrasenia) {

        U usuarioLogueado = obtenerUsuario(usuario, contrasenia);
        sesionHttp.setAttribute(getClaveSesion(), usuarioLogueado);
        return Command.lista(new Command("loginExitoso", siguienteCU()));
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

    static <T extends Usuario> T validarSesion(
            HttpSession sesionHttp, String clave, Class<T> tipo, String mensajeError) {
        Object usuario = sesionHttp.getAttribute(clave);
        if (tipo.isInstance(usuario)) {
            return tipo.cast(usuario);
        }
        throw new UsuarioException(mensajeError);
    }

    protected abstract String siguienteCU();

    protected abstract U obtenerUsuario(String nombreUsuario, String contrasenia);

    protected abstract String getLoginPage();

    protected abstract String getClaveSesion();
}
