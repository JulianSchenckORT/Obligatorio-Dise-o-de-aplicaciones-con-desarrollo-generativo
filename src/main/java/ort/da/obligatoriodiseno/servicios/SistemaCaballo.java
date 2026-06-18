package ort.da.obligatoriodiseno.servicios;

import java.util.ArrayList;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Caballo;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;

public class SistemaCaballo {
    private final List<Caballo> caballos = new ArrayList<>();

    public Caballo registrarCaballo(String nombre) {
        validarNombre(nombre);
        return registrarCaballo(new Caballo(nombre.trim()));
    }

    public Caballo registrarCaballo(Caballo caballo) {
        if (caballo == null) {
            throw new ApuestaException("Debe indicar un caballo");
        }
        validarNombre(caballo.getNombre());
        Caballo existente = buscarPorNombre(caballo.getNombre());
        if (existente != null) {
            return existente;
        }
        caballos.add(caballo);
        return caballo;
    }

    private Caballo buscarPorNombre(String nombre) {
        for (Caballo caballo : caballos) {
            if (caballo.getNombre().equalsIgnoreCase(nombre.trim())) {
                return caballo;
            }
        }
        return null;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ApuestaException("El nombre del caballo es obligatorio");
        }
    }
}
