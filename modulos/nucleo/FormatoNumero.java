package modulos.nucleo;

import java.math.BigDecimal;
import java.math.MathContext;

/** Utilidad para mostrar números de forma limpia (sin ceros ni ruido de punto flotante). */
public final class FormatoNumero {

    private FormatoNumero() {
    }

    public static String formatear(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        return new BigDecimal(v).round(new MathContext(12)).stripTrailingZeros().toPlainString();
    }
}
