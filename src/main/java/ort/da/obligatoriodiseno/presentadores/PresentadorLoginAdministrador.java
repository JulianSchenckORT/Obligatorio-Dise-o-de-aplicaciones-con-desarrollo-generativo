package ort.da.obligatoriodiseno.presentadores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.utils.Command;
import ort.da.obligatoriodiseno.utils.Commands;


@RestController
@RequestMapping("/login-admin")
public class PresentadorLoginAdministrador extends PresentadorLogin<Admin> {

    @PostMapping("/borrar-sesiones")
    public Commands borrarSesionesActivas() {
        fachada.borrarSesionesAdministradores();
        return Command.lista(new Command("mensaje", "Sesiones de administradores eliminadas"));
    }

    @Override
    protected String siguienteCU() {
        return "admin/tablero-administrador.html";
    }

    @Override
    protected Admin obtenerUsuario(String nombreUsuario, String contrasenia) {
        return fachada.loginAdministrador(nombreUsuario, contrasenia);
    }

    @Override
    protected String getLoginPage() {
        return "loginAdmin.html";
    }

    @Override
    protected String getClaveSesion() {
        return CLAVE_ADMINISTRADOR;
    }
}
