package ort.da.obligatoriodiseno.Dominio;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
@Getter
	@Setter
public class Hipodromo {
	
	private double comision;
	private List<Jornada> listaJornadas;

	public Hipodromo() {
		this(0.10);
	}

	public Hipodromo(double comision) {
		this.comision = comision;
		this.listaJornadas = new ArrayList<>();
	}

	public void agregarJornada(Jornada jornada) {
		if (!this.listaJornadas.contains(jornada)) {
			this.listaJornadas.add(jornada);
		}
	}

	public double getComisionByJornada(Jornada jornada) {
		return jornada.GetTotalApostado() * this.comision;
	}
}
