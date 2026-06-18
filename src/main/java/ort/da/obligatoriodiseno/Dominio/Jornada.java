package ort.da.obligatoriodiseno.Dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Jornada {

	private LocalDate fecha;
	private int contadorCarreras = 0;
	private List<Carrera> listaCarreras = new ArrayList<>();
	private Hipodromo hipodromo;

	public Jornada() {
	}

	public Jornada(LocalDate fecha) {
		this(fecha, new Hipodromo());
	}

	public Jornada(LocalDate fecha, Hipodromo hipodromo) {
		this.fecha = fecha;
		this.hipodromo = hipodromo;
		if (hipodromo != null) {
			hipodromo.agregarJornada(this);
		}
	}

	public Carrera agregarCarrera(String nombre) {
		contadorCarreras++;
		Carrera carrera = new Carrera(contadorCarreras, nombre, this);
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
		return GetTotalApostado() - GetTotalPagado();
	}

	public List<Carrera> GetCarreras() {
		return listaCarreras;
	}

	public List<Carrera> getCarrerasOrdenadas() {
		List<Carrera> carrerasOrdenadas = new ArrayList<>(listaCarreras);
		carrerasOrdenadas.sort(Comparator.comparingInt(Carrera::getNumero));
		return carrerasOrdenadas;
	}


	public Carrera getCarrera(int id) {
		for (Carrera carrera : listaCarreras) {
			if (carrera.getNumero() == id) {
				return carrera;
			}
		}
		return null;
	}
}
