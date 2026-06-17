package ort.da.obligatoriodiseno.Dominio;
import lombok.Getter;
import lombok.Setter;
import ort.da.obligatoriodiseno.Dominio.estadosApuesta.EnCurso;
	@Getter
	@Setter
public  class Apuesta {
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
	public void cambiarEstado(EstadoApuesta estado) {
		this.estadoApuesta = estado;
	}

	public Double calcularpago(double dividendo,double total) {
		return this.formaDeApostar.calcularPago(this.monto, dividendo, total);
	}

	public void pagar(double ganancias) {
		this.estadoApuesta.Pagar(ganancias, this.jugador);
	}
	public void confirmar() {
    this.estadoApuesta.confirmar(this);
	}

	public void descartar() {
	this.estadoApuesta.descartar(this);
	}
	public double calcularCosto() {
    return this.formaDeApostar.calcularCosto(this.monto);
	}
}
