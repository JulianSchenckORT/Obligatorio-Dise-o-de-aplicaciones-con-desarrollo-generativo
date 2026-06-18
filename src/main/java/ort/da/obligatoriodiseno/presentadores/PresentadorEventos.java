package ort.da.obligatoriodiseno.presentadores;

import java.io.IOException;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ort.da.obligatoriodiseno.eventos.EventoSistema;
import ort.da.obligatoriodiseno.eventos.ObservadorEvento;
import ort.da.obligatoriodiseno.eventos.PublicadorEventos;
import ort.da.obligatoriodiseno.utils.Command;

@RestController
@RequestMapping("/eventos")
public class PresentadorEventos {

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter escucharEventos() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        ObservadorEvento observador = new ObservadorEvento() {
            @Override
            public void alOcurrir(EventoSistema evento) {
                try {
                    Object respuesta = Objects.requireNonNull(
                            Command.lista(new Command("eventoSistema", evento)));
                    emitter.send(SseEmitter.event()
                            .data(respuesta, MediaType.APPLICATION_JSON));
                } catch (IOException | IllegalStateException e) {
                    PublicadorEventos.getInstancia().remover(this);
                    emitter.completeWithError(e);
                }
            }
        };

        PublicadorEventos.getInstancia().registrar(observador);
        emitter.onCompletion(() -> PublicadorEventos.getInstancia().remover(observador));
        emitter.onTimeout(() -> PublicadorEventos.getInstancia().remover(observador));
        emitter.onError((error) -> PublicadorEventos.getInstancia().remover(observador));

        return emitter;
    }
}
