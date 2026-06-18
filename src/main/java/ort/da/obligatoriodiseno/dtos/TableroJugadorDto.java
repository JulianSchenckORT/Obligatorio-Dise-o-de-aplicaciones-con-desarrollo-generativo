package ort.da.obligatoriodiseno.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TableroJugadorDto {
    private String nombreJugador;
    private String iniciales;
    private double saldoActual;
    private double totalApostado;
    private double totalGanado;
    private List<String> modalidadesApuesta = new ArrayList<>();
    private List<CarreraDto> carrerasDisponibles = new ArrayList<>();
    private List<ApuestaJugadorDto> misApuestas = new ArrayList<>();
}
