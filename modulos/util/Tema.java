package modulos.util;

import java.awt.Color;

/** Colores y constantes visuales centralizados. Cambia el tema editando solo este archivo. */
// Paleta de colores de la calculadora 
public final class Tema {

    private Tema() {
    }

    public static final Color FONDO = new Color(0x1B1D24);
    public static final Color PANTALLA = new Color(0x11131A);
    public static final Color TEXTO = new Color(0xECEFF4);
    public static final Color TEXTO_SUAVE = new Color(0x8E95A3);
    public static final Color TEXTO_ERROR = new Color(0xFF7B7B);

    public static final Color BTN_NUM = new Color(0x2E323E);
    public static final Color BTN_OP = new Color(0x3F4557);
    public static final Color BTN_FUN = new Color(0x2F4A5E);
    public static final Color BTN_IGUAL = new Color(0x2E8B6B);
    public static final Color BTN_LIMPIAR = new Color(0xB04A4A);

    public static final Color LIENZO = new Color(0x14161C);
    public static final Color CUADRICULA = new Color(0x262A34);
    public static final Color EJES = new Color(0x7A8194);
    public static final Color CAMPO = new Color(0x23262F);
    public static final Color ERROR = new Color(0x6B2A2A);

    // Color asignado para cada ranura de la insercion.
    public static final Color[] COLORES_FUNCION = {
        new Color(0x4FC3F7), new Color(0xFFB74D), new Color(0x81C784), new Color(0xF06292)
    };
}
