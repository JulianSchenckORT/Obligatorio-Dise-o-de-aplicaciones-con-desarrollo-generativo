package ort.da.obligatoriodiseno.presentadores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;


@RestController
@RequestMapping("/login-admin")
public class PresentadorLoginAdministrador extends PresentadorLogin<Admin> {

    @Override
    protected String siguienteCU() {
        return "admin/tablero-administrador.html";
    }

    @Override
    protected Admin obtenerUsuario(String nombreUsuario, String contrasenia) throws ApuestaException {
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
