package ort.da.obligatoriodiseno.dtos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TableroAdministradorDto {
    private String nombreAdministrador;
    private LocalDate fechaJornada;
    private double totalApostado;
    private double totalPagado;
    private double comisiones;
    private double balanceGeneral;
    private int carrerasTotales;
    private int carrerasFinalizadas;
    private int carrerasFaltanCorrer;
    private List<CarreraFinalizadaDto> carrerasFinalizadasDetalle = new ArrayList<>();
    private List<CarreraPendienteDto> proximasCarreras = new ArrayList<>();
}
