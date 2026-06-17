package ort.da.obligatoriodiseno.Dominio.estadosApuesta;
import ort.da.obligatoriodiseno.Dominio.EstadoApuesta;
import ort.da.obligatoriodiseno.Dominio.Jugador;
import ort.da.obligatoriodiseno.Dominio.Apuesta;
public class EnCurso implements EstadoApuesta {

	public void descartar(Apuesta apuesta) {
		apuesta.cambiarEstado(new Descartada());

	}

	public void confirmar(Apuesta apuesta) {
		apuesta.cambiarEstado(new Confirmada());
		apuesta.getNroRegistroCaballo().agregarApuesta(apuesta);
	}

	

	@Override
	public void Pagar(double ganancias,Jugador jugador) {
		// TODO Auto-generated method stub
		throw new IllegalStateException("Unimplemented method 'Pagar'");
	}
}

