package ort.da.obligatoriodiseno.servicios;

import java.util.ArrayList;
import java.util.List;

import ort.da.obligatoriodiseno.Dominio.Caballo;
import ort.da.obligatoriodiseno.excepciones.ApuestaException;

public class SistemaCaballo {
    private final List<Caballo> caballos = new ArrayList<>();

    public Caballo registrarCaballo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ApuestaException("El nombre del caballo es obligatorio");
        }
        Caballo existente = buscarPorNombre(nombre);
        if (existente != null) {
            return existente;
        }
        Caballo caballo = new Caballo(nombre.trim());
        caballos.add(caballo);
        return caballo;
    }

    public Caballo registrarCaballo(Caballo caballo) {
        if (caballo == null) {
            throw new ApuestaException("Debe indicar un caballo");
        }
        Caballo existente = buscarPorNombre(caballo.getNombre());
        if (existente != null) {
            return existente;
        }
        caballos.add(caballo);
        return caballo;
    }

    public Caballo buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        for (Caballo caballo : caballos) {
            if (caballo.getNombre().equalsIgnoreCase(nombre.trim())) {
                return caballo;
            }
        }
        return null;
    }

    public List<Caballo> getAllCaballos() {
        return List.copyOf(caballos);
    }
}
