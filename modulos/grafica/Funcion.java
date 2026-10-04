package modulos.grafica;

import java.awt.Color;
import modulos.nucleo.Evaluador;

/** Función y = f(x) ya compilada, con su color de trazo. */
public final class Funcion {

    private final String texto;
    private final Evaluador.Nodo nodo;
    private final Color color;

    public Funcion(String texto, Evaluador.Nodo nodo, Color color) {
        this.texto = texto;
        this.nodo = nodo;
        this.color = color;
    }

    public String getTexto() {
        return texto;
    }

    public Color getColor() {
        return color;
    }

    public double en(double x) {
        return nodo.eval(x);
    }
}
