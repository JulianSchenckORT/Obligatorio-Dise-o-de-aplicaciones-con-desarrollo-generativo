package ort.da.obligatoriodiseno.excepciones;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final int ERROR_APLICACION = 299;

    @ExceptionHandler(ApuestaException.class)
    public ResponseEntity<String> manejarApuestaException(ApuestaException ex) {
        return respuesta(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> manejarEstadoInvalido(IllegalStateException ex) {
        return respuesta(ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return respuesta("Falta completar el dato requerido: " + nombreParametro(ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> manejarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta("El dato ingresado no tiene un formato valido: " + nombreParametro(ex.getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErrorInesperado(Exception ex) {
        return respuesta("Ocurrio un error inesperado. Intente nuevamente.");
    }

    private ResponseEntity<String> respuesta(String mensaje) {
        return ResponseEntity.status(ERROR_APLICACION).body(mensaje);
    }

    private String nombreParametro(String nombreTecnico) {
        if ("contrasenia".equals(nombreTecnico)) {
            return "contraseña";
        }
        if ("nroCarrera".equals(nombreTecnico)) {
            return "número de carrera";
        }
        if ("nroCaballo".equals(nombreTecnico)) {
            return "número de caballo";
        }
        if ("caballoGanador".equals(nombreTecnico)) {
            return "caballo ganador";
        }
        if ("tipoApuesta".equals(nombreTecnico)) {
            return "tipo de apuesta";
        }
        return nombreTecnico;
    }
}
