package ort.da.obligatoriodiseno.servicios;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.SesionActiva;
import ort.da.obligatoriodiseno.Dominio.Usuario;
import ort.da.obligatoriodiseno.dtos.TableroJugadorDto;
import ort.da.obligatoriodiseno.excepciones.UsuarioException;
import ort.da.obligatoriodiseno.utils.TextoUtils;

public class SistemaUsuarios {
    private final List<Usuario> usuarios;
    private final Collection<SesionActiva> sesiones;
    private final SistemaCarrera sistemaCarrera;
    private final SistemaApuestas sistemaApuestas;

    public SistemaUsuarios(SistemaCarrera sistemaCarrera, SistemaApuestas sistemaApuestas) {
        this.usuarios = new ArrayList<>();
        this.sesiones = new ArrayList<>();
        this.sistemaCarrera = sistemaCarrera;
        this.sistemaApuestas = sistemaApuestas;
    }

    public void registrarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new UsuarioException("Debe indicar un usuario");
        }
        boolean existe = usuarios.stream()
                .anyMatch(registrado -> registrado.getUsername().equalsIgnoreCase(usuario.getUsername()));
        if (existe) {
            throw new UsuarioException("Ya existe un usuario con ese nombre");
        }
        usuarios.add(usuario);
    }

    public TableroJugadorDto obtenerTableroJugador(Jugador jugador) {
        TableroJugadorDto tablero = new TableroJugadorDto();
        tablero.setNombreJugador(jugador.getNombre());
        tablero.setIniciales(TextoUtils.obtenerIniciales(jugador.getNombre()));
        tablero.setSaldoActual(jugador.getSaldo());
        tablero.setTotalApostado(jugador.calcularTotalApostado());
        tablero.setTotalGanado(jugador.getGanancias());
        tablero.setModalidadesApuesta(sistemaApuestas.obtenerNombresModalidades());

        for (Carrera carrera : sistemaCarrera.getCarrerasDisponibles()) {
            tablero.getCarrerasDisponibles().add(sistemaCarrera.crearCarreraDto(carrera));
        }
        for (Apuesta apuesta : sistemaApuestas.obtenerApuestasOrdenadas(jugador)) {
            tablero.getMisApuestas().add(sistemaApuestas.crearApuestaJugadorDto(apuesta));
        }
        return tablero;
    }

    private Usuario login(String nombre, String contrasenia) {
        if (nombre == null || nombre.isBlank() || contrasenia == null || contrasenia.isBlank()) {
            throw new UsuarioException("Acceso denegado");
        }
        for (Usuario usuario : usuarios) {
            if (usuario.esPasswordDe(nombre, contrasenia)) {
                return usuario;
            }
        }
        throw new UsuarioException("Acceso denegado");
    }

    public Admin loginAdministrador(String nombre, String contrasenia) {
        Usuario usuario = login(nombre, contrasenia);
        if (!(usuario instanceof Admin admin)) {
            throw new UsuarioException("Acceso denegado");
        }
        if (tieneSesionActiva(admin)) {
            throw new UsuarioException("El administrador ya tiene una sesión activa");
        }
        sesiones.add(new SesionActiva(admin));
        return admin;
    }

    public Jugador loginJugador(String nombre, String contrasenia) {
        Usuario usuario = login(nombre, contrasenia);
        if (!(usuario instanceof Jugador jugador)) {
            throw new UsuarioException("Acceso denegado");
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
        sesiones.removeIf(sesion -> sesion.getUsuario().equals(usuario));
    }

    public void borrarSesionesAdministradores() {
        sesiones.removeIf(sesion -> sesion.getUsuario() instanceof Admin);
    }

}
