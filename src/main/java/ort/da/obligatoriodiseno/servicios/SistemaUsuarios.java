package ort.da.obligatoriodiseno.servicios;
import java.util.List;
import java.util.Collection;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.SesionActiva;
import ort.da.obligatoriodiseno.Dominio.Usuario;

public class SistemaUsuarios {

	private List<Usuario> usuarios;
	private Collection<SesionActiva> sesiones;
	private Collection<SesionActiva> sesionesAdmin;

	public Apuesta GetApuestaByJugador(int jugador) {
		return null;
	}

	private int login(String nombre, String contrasenia) {
		return 0;
	}

	public Admin loginAdministrador(String nombre, String contrasenia) {
		return null;
	}

	public Jugador loginJugador(String nombre, String contrasenia) {
		return null;
	}

	 public void logout(Usuario usuario) {

        SesionActiva sesionABorrar = null;

        for (SesionActiva sesion : sesiones) {
            if (sesion.getUsuario().equals(usuario)) {
                sesionABorrar = sesion;
            }
        }

        if (sesionABorrar != null) {
            sesiones.remove(sesionABorrar);
        }
    }

	 public Collection<SesionActiva> getSesiones() {
	    return  null;}
    }


