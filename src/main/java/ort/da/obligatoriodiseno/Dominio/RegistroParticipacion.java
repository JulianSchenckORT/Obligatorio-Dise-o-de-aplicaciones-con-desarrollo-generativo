package ort.da.obligatoriodiseno.Dominio;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
@Getter
	@Setter
public class RegistroParticipacion {

	private double dividendo;

	private int Id;

	private List<Apuesta> listaApuestas;

	private Carrera carrera;

	private Caballo caballo;

	public void calcularDividendo() {
		this.dividendo = carrera.calcularDividendo();
	}
	public double getTotalApostado() {
    double total = 0;

    for (Apuesta apuesta : this.listaApuestas) {
        total += apuesta.calcularCosto();
    }

    return total;
	}
	public double calcularPago() {
		return 0;
	}
	public void agregarApuesta(Apuesta apuesta) {
    this.listaApuestas.add(apuesta);
    this.carrera.recalcularDividendos();
}

}
