package ort.da.obligatoriodiseno.Dominio;

public interface EstadoCarrera {

    void finalizar(Carrera carrera, RegistroParticipacion caballo);

    void abrir(Carrera carrera);

    void cerrar(Carrera carrera);
    void verificarDividendos(Carrera carrera);
}
