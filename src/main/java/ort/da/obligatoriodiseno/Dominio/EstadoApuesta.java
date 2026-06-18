package ort.da.obligatoriodiseno.Dominio;
public interface EstadoApuesta {
    void confirmar(Apuesta apuesta);
    void descartar(Apuesta apuesta);
    void pagar(double ganancias, Jugador jugador);
}
