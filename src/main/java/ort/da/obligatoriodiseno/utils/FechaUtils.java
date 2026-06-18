package ort.da.obligatoriodiseno.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public final class FechaUtils {
    private FechaUtils() {
    }

    public static LocalDate toLocalDate(Date fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
