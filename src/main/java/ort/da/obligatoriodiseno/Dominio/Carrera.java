package ort.da.obligatoriodiseno.Dominio;
import java.sql.Date;
import java.util.List;

public class Carrera {

	private int jornadaId;

	private int numero;

	private String nombre;

	private List<RegistroParticipacion> caballos;

	private Date HoraFinal;

	private RegistroParticipacion ganador;

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
