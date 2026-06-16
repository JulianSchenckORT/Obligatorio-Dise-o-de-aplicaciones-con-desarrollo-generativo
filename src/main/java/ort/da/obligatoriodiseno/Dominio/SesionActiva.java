package ort.da.obligatoriodiseno.Dominio;

import lombok.Getter;

@Getter
public class SesionActiva {

    private Usuario usuario;

    public SesionActiva(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombreUsuario() {
        return usuario.getUsername();
    }

    public String getNombre() {
        return usuario.getNombre();
    }

    public boolean esAdministrador() {
        return usuario instanceof Admin;
    }

    public boolean esJugador() {
        return usuario instanceof Jugador;
    }
}