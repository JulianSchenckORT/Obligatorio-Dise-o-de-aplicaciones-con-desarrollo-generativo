package ort.da.obligatoriodiseno.servicios;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.SesionActiva;
import ort.da.obligatoriodiseno.Dominio.Usuario;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;

public class SistemaUsuarios {

    private List<Usuario> usuarios;
    private Collection<SesionActiva> sesiones;

    public SistemaUsuarios() {
        this.usuarios = new ArrayList<>();
        this.sesiones = new ArrayList<>();
        precargarUsuarios();
    }
// las precargas van en obligatoriodisenoApplication
    private void precargarUsuarios() {
        usuarios.add(new Admin("a1", "a1", "Usuario Administrador"));
        usuarios.add(new Admin("a2", "a2", "Segundo Administrador"));
        usuarios.add(new Jugador("j1", "j1", "Usuario Jugador", 2000));
        usuarios.add(new Jugador("j2", "j2", "Segundo Jugador", 3000));
    }

    public Apuesta GetApuestaByJugador(int jugador) {
        return null;
    }

    private Usuario login(String nombre, String contrasenia) throws ApuestaException {
        if (nombre == null || nombre.isBlank() || contrasenia == null || contrasenia.isBlank()) {
            throw new ApuestaException("Debe ingresar usuario y contrasena");
        }

        for (Usuario usuario : usuarios) {
            if (usuario.esPasswordDe(nombre, contrasenia)) {
                return usuario;
            }
        }

        throw new ApuestaException("Usuario o contrasena incorrectos");
    }

    public Admin loginAdministrador(String nombre, String contrasenia) throws ApuestaException {
        Usuario usuario = login(nombre, contrasenia);
        if (!(usuario instanceof Admin admin)) {
            throw new ApuestaException("El usuario ingresado no es administrador");
        }
        if (tieneSesionActiva(admin)) {
            throw new ApuestaException("El administrador ya tiene una sesion activa");
        }
        sesiones.add(new SesionActiva(admin));
        return admin;
    }

    public Jugador loginJugador(String nombre, String contrasenia) throws ApuestaException {
        Usuario usuario = login(nombre, contrasenia);
        if (!(usuario instanceof Jugador jugador)) {
            throw new ApuestaException("El usuario ingresado no es jugador");
        }
        if (!tieneSesionActiva(jugador)) {
            sesiones.add(new SesionActiva(jugador));
        }
        return jugador;
    }

    private boolean tieneSesionActiva(Usuario usuario) {
        for (SesionActiva sesion : sesiones) {
            if (sesion.getUsuario().equals(usuario)) {
                return true;
            }
        }
        return false;
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

    public void cerrarSesionesAdministradores() {
        sesiones.removeIf(SesionActiva::esAdministrador);
    }

    public Collection<SesionActiva> getSesiones() {
        return sesiones;
    }
}