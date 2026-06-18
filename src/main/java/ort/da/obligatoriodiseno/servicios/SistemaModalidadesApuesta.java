package ort.da.obligatoriodiseno.servicios;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ort.da.obligatoriodiseno.Dominio.FormaDeApostar;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;

public class SistemaModalidadesApuesta {
    private final Map<String, FormaDeApostar> modalidades = new LinkedHashMap<>();

    public void registrar(FormaDeApostar modalidad) {
        if (modalidad == null || modalidad.getNombre() == null || modalidad.getNombre().isBlank()) {
            throw new ApuestaException("La modalidad de apuesta es inválida");
        }
        modalidades.put(normalizar(modalidad.getNombre()), modalidad);
    }

    public FormaDeApostar obtener(String nombre) {
        FormaDeApostar modalidad = modalidades.get(normalizar(nombre));
        if (modalidad == null) {
            throw new ApuestaException("Tipo de apuesta inválido");
        }
        return modalidad;
    }

    public List<String> obtenerNombres() {
        List<String> nombres = new ArrayList<>();
        for (FormaDeApostar modalidad : modalidades.values()) {
            nombres.add(modalidad.getNombre());
        }
        return List.copyOf(nombres);
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
