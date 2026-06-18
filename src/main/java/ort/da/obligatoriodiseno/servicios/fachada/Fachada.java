package ort.da.obligatoriodiseno.servicios.fachada;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Admin;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
import ort.da.obligatoriodiseno.Dominio.Caballo;
import ort.da.obligatoriodiseno.Dominio.Carrera;
import ort.da.obligatoriodiseno.Dominio.Jornada;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.FormaDeApostar;
import ort.da.obligatoriodiseno.Dominio.Usuario;
import ort.da.obligatoriodiseno.dtos.ApuestaEnCursoDto;
import ort.da.obligatoriodiseno.dtos.CarreraDto;
import ort.da.obligatoriodiseno.dtos.TableroAdministradorDto;
import ort.da.obligatoriodiseno.dtos.TableroJugadorDto;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;
import ort.da.obligatoriodiseno.servicios.SistemaApuestas;
import ort.da.obligatoriodiseno.servicios.SistemaCaballo;
import ort.da.obligatoriodiseno.servicios.SistemaCarrera;
import ort.da.obligatoriodiseno.servicios.SistemaHipodromo;
import ort.da.obligatoriodiseno.servicios.SistemaUsuarios;

public class Fachada {
    private static Fachada instancia;

    private final SistemaUsuarios sistemaUsuarios;
    private final SistemaCarrera sistemaCarrera;
    private final SistemaHipodromo sistemaHipodromo;
    private final SistemaCaballo sistemaCaballo;
    private final SistemaApuestas sistemaApuestas;

    private Fachada() {
        sistemaHipodromo = new SistemaHipodromo();
        sistemaCaballo = new SistemaCaballo();
        sistemaCarrera = new SistemaCarrera(sistemaHipodromo, sistemaCaballo);
        sistemaApuestas = new SistemaApuestas(sistemaCarrera);
        sistemaUsuarios = new SistemaUsuarios(sistemaCarrera, sistemaApuestas);
    }

    public static Fachada getInstancia() {
        if (instancia == null) {
            instancia = new Fachada();
        }
        return instancia;
    }

    public void registrarUsuario(Usuario usuario) {
        sistemaUsuarios.registrarUsuario(usuario);
    }

    public Jornada registrarJornada(LocalDate fecha) {
        return sistemaHipodromo.registrarJornada(fecha);
    }

    public Carrera registrarCarrera(LocalDate fecha, String nombre) {
        return sistemaCarrera.agregarCarrera(fecha, nombre);
    }

    public Caballo registrarCaballo(String nombre) {
        return sistemaCaballo.registrarCaballo(nombre);
    }

    public void agregarParticipante(Caballo caballo, Carrera carrera) {
        sistemaCarrera.agregarParticipante(caballo, carrera);
    }

    public List<Caballo> obtenerCaballos() {
        return sistemaCaballo.getAllCaballos();
    }

    public void registrarModalidad(FormaDeApostar modalidad) {
        sistemaApuestas.registrarModalidad(modalidad);
    }

    public Admin loginAdministrador(String nombreUsuario, String contrasenia) throws ApuestaException {
        return sistemaUsuarios.loginAdministrador(nombreUsuario, contrasenia);
    }

    public Jugador loginJugador(String nombreUsuario, String contrasenia) throws ApuestaException {
        return sistemaUsuarios.loginJugador(nombreUsuario, contrasenia);
    }

    public void logout(Usuario usuario) {
        sistemaUsuarios.logout(usuario);
    }

    public Collection<?> getSesionesActivas() {
        return sistemaUsuarios.getSesiones();
    }

    public Carrera getCarrera(Date fecha, int id) {
        return sistemaCarrera.getCarrera(fecha, id);
    }

    public TableroAdministradorDto obtenerTableroAdministrador() throws ApuestaException {
        return sistemaHipodromo.obtenerTableroAdministrador();
    }

    public TableroAdministradorDto obtenerTableroAdministrador(LocalDate fecha) throws ApuestaException {
        return sistemaHipodromo.obtenerTableroAdministrador(fecha);
    }

    public TableroAdministradorDto obtenerTableroJornadaAnterior(LocalDate fecha) throws ApuestaException {
        return sistemaHipodromo.obtenerTableroJornadaAnterior(fecha);
    }

    public TableroAdministradorDto obtenerTableroJornadaSiguiente(LocalDate fecha) throws ApuestaException {
        return sistemaHipodromo.obtenerTableroJornadaSiguiente(fecha);
    }

    public CarreraDto obtenerCarreraParaGestion(LocalDate fecha, int numero) throws ApuestaException {
        return sistemaCarrera.obtenerCarreraParaGestion(fecha, numero);
    }

    public CarreraDto abrirCarrera(LocalDate fecha, int numero) throws ApuestaException {
        return sistemaCarrera.abrirCarrera(fecha, numero);
    }

    public CarreraDto cerrarCarrera(LocalDate fecha, int numero) throws ApuestaException {
        return sistemaCarrera.cerrarCarrera(fecha, numero);
    }

    public CarreraDto finalizarCarrera(LocalDate fecha, int numero, Integer caballoGanador) throws ApuestaException {
        return sistemaCarrera.finalizarCarrera(fecha, numero, caballoGanador);
    }

    public TableroJugadorDto obtenerTableroJugador(Jugador jugador) {
        return sistemaUsuarios.obtenerTableroJugador(jugador);
    }

    public Apuesta prepararApuesta(Jugador jugador, LocalDate fecha, int nroCarrera, int nroCaballo,
            double monto, String tipoApuesta) throws ApuestaException {
        return sistemaApuestas.prepararApuesta(jugador, fecha, nroCarrera, nroCaballo, monto, tipoApuesta);
    }

    public ApuestaEnCursoDto obtenerApuestaEnCurso(Apuesta apuesta) {
        return sistemaApuestas.obtenerApuestaEnCurso(apuesta);
    }

    public void confirmarApuesta(Jugador jugador, Apuesta apuesta, String contrasenia) throws ApuestaException {
        sistemaApuestas.confirmarApuesta(jugador, apuesta, contrasenia);
    }

    public void descartarApuesta(Jugador jugador, Apuesta apuesta) {
        sistemaApuestas.descartarApuesta(jugador, apuesta);
    }
}
