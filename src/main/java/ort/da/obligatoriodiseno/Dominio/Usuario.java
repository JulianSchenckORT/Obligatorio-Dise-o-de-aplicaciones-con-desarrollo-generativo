package ort.da.obligatoriodiseno.Dominio;
import lombok.Getter;
public abstract class Usuario {
	@Getter
	private String username;

	private String password;
	@Getter
	private String nombre;

	public boolean esPasswordDe(String username, String password) {
		return false;
	}

}
