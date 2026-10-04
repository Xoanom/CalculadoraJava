package modulos.nucleo;

/** Error de sintaxis o de evaluación de una expresión matemática. */
public class ExpresionException extends Exception {
    public ExpresionException(String mensaje) {
        super(mensaje);
    }
}
