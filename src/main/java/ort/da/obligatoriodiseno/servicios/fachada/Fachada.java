package ort.da.obligatoriodiseno.servicios.fachada;
import java.util.Collection;
import java.util.Date;
import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.Usuario;
import ort.da.obligatoriodiseno.Dominio.SesionActiva;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.servicios.SistemaUsuarios;
import ort.da.obligatoriodiseno.servicios.SistemaCarrera;
import ort.da.obligatoriodiseno.servicios.SistemaCaballo;
import ort.da.obligatoriodiseno.servicios.SistemaHipodromo;

public class Fachada {
private static Fachada instancia;

    private SistemaUsuarios sUsuarios;
    private SistemaCarrera sCarrera;
    private SistemaCaballo sCaballo;
    private SistemaHipodromo sHipodromo;

    private Fachada() {
        sUsuarios = new SistemaUsuarios();
        sCarrera = new SistemaCarrera();
        sCaballo = new SistemaCaballo();
        sHipodromo = new SistemaHipodromo();
    }

    public static Fachada getInstancia() {
        if (instancia == null) {
            instancia = new Fachada();
        }
        return instancia;
    }

    public Admin loginAdministrador(String nombreUsuario, String contrasenia) throws ApuestaException {
        return sUsuarios.loginAdministrador(nombreUsuario, contrasenia);
    }

    public Jugador loginJugador(String nombreUsuario, String contrasenia) throws ApuestaException {
        return sUsuarios.loginJugador(nombreUsuario, contrasenia);
    }

   public void logout(Usuario usuario) {
		sUsuarios.logout(usuario);
	}

    public Collection<?> getSesionesActivas() {
        return sUsuarios.getSesiones();
    }

	public void cargarjornadaFecha(Date fecha) {

	}

	public Carrera getCarrera(Date fecha, int id) {
		return null;
	}

}
