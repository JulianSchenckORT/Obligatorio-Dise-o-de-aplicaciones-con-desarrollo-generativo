package ort.da.obligatoriodiseno.Dominio;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ort.da.obligatoriodiseno.Dominio.EstadoCarrera;
import ort.da.obligatoriodiseno.Dominio.RegistroParticipacion;
import ort.da.obligatoriodiseno.Dominio.estadosCarrera.Definida;
import ort.da.obligatoriodiseno.Dominio.Caballo;

import java.time.LocalDate;
import java.time.LocalTime;
public class Carrera {
	@Getter
	@Setter
	private Jornada jornada;
	private int numero;
	private String nombre;	
	private List<RegistroParticipacion> caballos;
	private LocalTime HoraFinal;
	private RegistroParticipacion ganador;
	private EstadoCarrera estado;

	public Carrera(int nroCarrera, String nombre, Jornada jornada, Jornada jornada) {
        this.numero = nroCarrera;
        this.nombre = nombre;
        this.jornada = jornada;
        this.caballos = new ArrayList<>();
		this.estado = new Definida();
		 this.HoraFinal = null;
		this.ganador = null;
		
    }

	public void finalizar(RegistroParticipacion caballoId) {

	}

	public double getTotalPagado() {
		return 0;
	}

	public void totalApostadoporCaballo(RegistroParticipacion caballo) {

	}

	public double calcularDividendo() {
		return 0;
	}

	public void cambiarEstado(Estado state) {

	}

}
