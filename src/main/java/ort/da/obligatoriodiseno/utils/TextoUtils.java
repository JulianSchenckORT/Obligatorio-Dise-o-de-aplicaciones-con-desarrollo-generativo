package ort.da.obligatoriodiseno.utils;

public final class TextoUtils {
    private TextoUtils() {
    }

    public static String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "--";
        }
        String[] partes = nombre.trim().split("\\s+");
        String primera = partes[0].substring(0, 1);
        String segunda = partes.length > 1 ? partes[1].substring(0, 1) : "";
        return (primera + segunda).toUpperCase();
    }
}
