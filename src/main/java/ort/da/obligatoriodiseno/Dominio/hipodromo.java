package ort.da.obligatoriodiseno.Dominio;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
@Getter
	@Setter
public class Hipodromo {
	
	private double comision;
	private List<Jornada> listaJornadas;

	public Double GetTotalApostadoByJornada(Jornada jornada) {
		return jornada.GetTotalApostado();
	}

	public double CalcularBalanceByJornada(Jornada jornada) {
		return jornada.CalcularBalance();
	}

	public List<Carrera> getCarrerasByJornada(Jornada jornada) {
		return jornada.getListaCarreras();
	}

	public double getComisionByJornada(Jornada jornada) {
		return jornada.GetTotalApostado() * this.comision;
	}

	public Double getTotalPagadoByJornada(Jornada jornada) {
		return jornada.GetTotalPagado();
	}

}
