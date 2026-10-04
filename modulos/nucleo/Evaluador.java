package modulos.nucleo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Analizador de expresiones (descenso recursivo).
 * Compila el texto una sola vez a un árbol de {@link Nodo} que luego puede
 * evaluarse miles de veces (ideal para graficar).
 *
 * Soporta: + - * / ^ % ( ), multiplicación implícita (2x, 3(x+1)),
 * variable x, constantes pi y e, y funciones:
 * sqrt, cbrt, log (base 10), ln, exp, abs, sin, cos, tan, asin, acos, atan.
 * Trigonométricas en radianes.
 */
public final class Evaluador {

    /** Expresión ya compilada, evaluable para un valor de x. */
    @FunctionalInterface
    public interface Nodo {
        double eval(double x);
    }

    private static final Map<String, DoubleUnaryOperator> FUNCIONES = new HashMap<>();

    static {
        FUNCIONES.put("sqrt", Math::sqrt);
        FUNCIONES.put("cbrt", Math::cbrt);
        FUNCIONES.put("log", Math::log10);
        FUNCIONES.put("ln", Math::log);
        FUNCIONES.put("exp", Math::exp);
        FUNCIONES.put("abs", Math::abs);
        FUNCIONES.put("sin", Math::sin);
        FUNCIONES.put("cos", Math::cos);
        FUNCIONES.put("tan", Math::tan);
        FUNCIONES.put("asin", Math::asin);
        FUNCIONES.put("acos", Math::acos);
        FUNCIONES.put("atan", Math::atan);
    }

    private final String s;
    private int pos;

    private Evaluador(String texto) {
        this.s = texto.replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT)
                .replace(',', '.')
                .replace("π", "pi")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replaceAll("√(\\d+\\.?\\d*)", "sqrt($1)")
                .replace("√", "sqrt");
    }

    // ---------------------------------------------------------------- API

    public static Nodo compilar(String texto) throws ExpresionException {
        Evaluador ev = new Evaluador(texto);
        if (ev.s.isEmpty()) {
            throw new ExpresionException("Expresión vacía");
        }
        Nodo nodo = ev.expresion();
        if (ev.pos < ev.s.length()) {
            char c = ev.s.charAt(ev.pos);
            throw new ExpresionException(c == ')' ? "Paréntesis ')' sin abrir" : "Símbolo inesperado: '" + c + "'");
        }
        return nodo;
    }

    public static double evaluar(String texto) throws ExpresionException {
        return compilar(texto).eval(0);
    }

    // ------------------------------------------------------------ Gramática
    // expresion := termino (('+'|'-') termino)*
    // termino   := unario (('*'|'/'|implícita) unario)*
    // unario    := ('-'|'+') unario | potencia
    // potencia  := postfijo ('^' unario)?
    // postfijo  := primario '%'*
    // primario  := numero | '(' expresion ')' | funcion '(' expresion ')' | constante | x

    private Nodo expresion() throws ExpresionException {
        Nodo izq = termino();
        while (pos < s.length()) {
            char c = s.charAt(pos);
            final Nodo a = izq;
            if (c == '+') {
                pos++;
                final Nodo b = termino();
                izq = v -> a.eval(v) + b.eval(v);
            } else if (c == '-') {
                pos++;
                final Nodo b = termino();
                izq = v -> a.eval(v) - b.eval(v);
            } else {
                break;
            }
        }
        return izq;
    }

    private Nodo termino() throws ExpresionException {
        Nodo izq = unario();
        while (pos < s.length()) {
            char c = s.charAt(pos);
            final Nodo a = izq;
            if (c == '*') {
                pos++;
                final Nodo b = unario();
                izq = v -> a.eval(v) * b.eval(v);
            } else if (c == '/') {
                pos++;
                final Nodo b = unario();
                izq = v -> a.eval(v) / b.eval(v);
            } else if (Character.isDigit(c) || c == '.' || Character.isLetter(c) || c == '(') {
                final Nodo b = unario(); // multiplicación implícita
                izq = v -> a.eval(v) * b.eval(v);
            } else {
                break;
            }
        }
        return izq;
    }

    private Nodo unario() throws ExpresionException {
        if (pos < s.length() && s.charAt(pos) == '-') {
            pos++;
            final Nodo a = unario();
            return v -> -a.eval(v);
        }
        if (pos < s.length() && s.charAt(pos) == '+') {
            pos++;
            return unario();
        }
        return potencia();
    }

    private Nodo potencia() throws ExpresionException {
        final Nodo base = postfijo();
        if (pos < s.length() && s.charAt(pos) == '^') {
            pos++;
            final Nodo exp = unario(); // asociativa a la derecha
            return v -> Math.pow(base.eval(v), exp.eval(v));
        }
        return base;
    }

    private Nodo postfijo() throws ExpresionException {
        Nodo n = primario();
        while (pos < s.length() && s.charAt(pos) == '%') {
            pos++;
            final Nodo a = n;
            n = v -> a.eval(v) / 100.0;
        }
        return n;
    }

    private Nodo primario() throws ExpresionException {
        if (pos >= s.length()) {
            throw new ExpresionException("La expresión está incompleta");
        }
        char c = s.charAt(pos);
        if (Character.isDigit(c) || c == '.') {
            return numero();
        }
        if (c == '(') {
            pos++;
            Nodo n = expresion();
            cerrarParentesis();
            return n;
        }
        if (Character.isLetter(c)) {
            return identificador();
        }
        throw new ExpresionException("Símbolo inesperado: '" + c + "'");
    }

    private Nodo numero() throws ExpresionException {
        int ini = pos;
        while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) {
            pos++;
        }
        try {
            final double valor = Double.parseDouble(s.substring(ini, pos));
            return v -> valor;
        } catch (NumberFormatException ex) {
            throw new ExpresionException("Número mal formado");
        }
    }

    private Nodo identificador() throws ExpresionException {
        int ini = pos;
        while (pos < s.length() && Character.isLetter(s.charAt(pos))) {
            pos++;
        }
        String nombre = s.substring(ini, pos);

        if (pos < s.length() && s.charAt(pos) == '(') {
            final DoubleUnaryOperator f = FUNCIONES.get(nombre);
            if (f == null) {
                throw new ExpresionException("Función desconocida: " + nombre);
            }
            pos++;
            final Nodo arg = expresion();
            cerrarParentesis();
            return v -> f.applyAsDouble(arg.eval(v));
        }
        switch (nombre) {
            case "x":
                return v -> v;
            case "pi":
                return v -> Math.PI;
            case "e":
                return v -> Math.E;
            default:
                throw new ExpresionException("Símbolo desconocido: " + nombre);
        }
    }

    /** Cierra un paréntesis; si la expresión terminó, se cierra automáticamente. */
    private void cerrarParentesis() throws ExpresionException {
        if (pos < s.length() && s.charAt(pos) == ')') {
            pos++;
        } else if (pos < s.length()) {
            throw new ExpresionException("Falta cerrar un paréntesis");
        }
    }
}
