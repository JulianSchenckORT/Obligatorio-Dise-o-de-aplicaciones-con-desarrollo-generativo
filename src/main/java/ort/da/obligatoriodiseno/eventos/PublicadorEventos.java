package ort.da.obligatoriodiseno.eventos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PublicadorEventos {
    private static final PublicadorEventos INSTANCIA = new PublicadorEventos();

    private final List<ObservadorEvento> observadores = new CopyOnWriteArrayList<>();

    private PublicadorEventos() {
    }

    public static PublicadorEventos getInstancia() {
        return INSTANCIA;
    }

    public void registrar(ObservadorEvento observador) {
        observadores.add(observador);
    }

    public void remover(ObservadorEvento observador) {
        observadores.remove(observador);
    }

    public void notificar(EventoSistema evento) {
        for (ObservadorEvento observador : observadores) {
            observador.alOcurrir(evento);
        }
    }

    public void notificarTablerosActualizados() {
        notificar(new EventoSistema("TABLEROS_ACTUALIZADOS", null));
    }
}
