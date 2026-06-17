package ort.da.obligatoriodiseno.Dominio;
import lombok.Getter;
import lombok.Setter;
import ort.da.obligatoriodiseno.Dominio.estadosApuesta.EnCurso;
public abstract class Apuesta {
	@Getter
	@Setter
	private double monto;
	private RegistroParticipacion NroRegistroCaballo;
	private Jugador jugador;
	private EstadoApuesta estadoApuesta;
    private FormaDeApostar formaDeApostar;
	public Apuesta(double monto, RegistroParticipacion nroRegistroCaballo, Jugador jugador,
                   FormaDeApostar formaDeApostar) {
        this.monto = monto;
        this.NroRegistroCaballo = nroRegistroCaballo;
        this.jugador = jugador;
        this.formaDeApostar = formaDeApostar;
        this.estadoApuesta = new EnCurso();
    }
	public void cambiarEstado() {

	}

	public Double calcularpago() {
		return null;
	}

	public void pagar(double ganancias) {

	}

}
