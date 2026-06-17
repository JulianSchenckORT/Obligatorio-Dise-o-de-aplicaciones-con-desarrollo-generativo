package ort.da.obligatoriodiseno.Dominio;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
@Getter
	@Setter
public class Jornada {

	private Date fecha;
	private int contadorCarreras = 0;
	private List<Carrera> listaCarreras = new ArrayList<>();

	public Carrera agregarCarrera(String nombre) {
        // incrementa el contador antes de asignar
        contadorCarreras++;
        Carrera carrera = new Carrera(contadorCarreras, nombre, this);

        // la lista de caballos queda vacía porque Carrera ya inicializa caballos = new ArrayList<>();
        this.listaCarreras.add(carrera);
        return carrera;
    }

	public Double GetTotalApostado() {
		double total = 0;
		for (Carrera carrera : this.listaCarreras) {
			total += carrera.getTotalApostado();
		}
		return total;
	}

	public double GetTotalPagado() {
		double total = 0;
		for (Carrera carrera : this.listaCarreras) {
			total += carrera.getTotalPagado();
		}
		return total;
	}

	public Double CalcularBalance() {
		return this.GetTotalApostado() - this.GetTotalPagado();
	}

	public Carrera getCarrera(int id) {
		return null;
	}

}
