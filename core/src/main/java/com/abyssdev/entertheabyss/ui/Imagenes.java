package com.abyssdev.entertheabyss.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/**
 * Gestor estático de todas las imágenes del juego.
 * Carga todas las texturas una sola vez y las mantiene en memoria.
 *
 * USO:
 * - Llamar a ImageManager.cargar() al inicio del juego (en create())
 * - Usar ImageManager.getFondo() o cualquier método get para obtener texturas
 * - Llamar a ImageManager.dispose() al cerrar el juego
 */
public class Imagenes {

    // ===== FONDOS =====
    private static Texture fondoMenu;
    private static Texture fondoArbol;
    private static Texture fondoTienda;
    private static Texture fondoPausa;
    private static Texture fondoGameOver;
    private static Texture fondoWin;

    // ===== PERSONAJES =====
    private static Texture spriteJugador;
    private static Texture spriteEnemigo;

    // ===== HUD - CORAZONES =====
    private static Texture corazon100;
    private static Texture corazon75;
    private static Texture corazon50;
    private static Texture corazon25;
    private static Texture moneda;

    // ===== HABILIDADES - ICONOS =====
    private static Texture iconoEspada;
    private static Texture iconoEspadaDoble;
    private static Texture iconoEspadaRoja;
    private static Texture iconoBotas;
    private static Texture iconoBotas2;
    private static Texture iconoBotasDoradas;
    private static Texture iconoCorazon;
    private static Texture iconoCorazonDorado;
    private static Texture iconoEscudo;

    // ===== TUTORIALES =====
    private static Texture tutorialMovimiento;
    private static Texture tutorialCombate;
    private static Texture tutorialOgrini;
    private static Texture tutorialArbol;
    private static Texture tutorialObjetivo;

    // Control de inicialización
    private static boolean cargado = false;

    /**
     * Carga TODAS las imágenes del juego.
     * Debe llamarse UNA SOLA VEZ al inicio (en EnterTheAbyssPrincipal.create())
     */
    public static void cargar() {
        if (cargado) {
            System.out.println("⚠️ ImageManager ya fue cargado");
            return;
        }

        System.out.println("📦 Cargando todas las imágenes del juego...");

        // === FONDOS ===
        fondoMenu = cargarTextura("Fondos/fondoMenu.png", true);
        fondoArbol = cargarTextura("Fondos/FondoArbol.PNG", true);
        fondoTienda = cargarTextura("Fondos/OgroTienda3.png", true);
        fondoPausa = cargarTextura("Fondos/pausa2.PNG", true);
        fondoGameOver = cargarTextura("Fondos/gameover.png", true);
        fondoWin = cargarTextura("Fondos/Win1.jpg", true);

        // === PERSONAJES ===
        spriteJugador = cargarTextura("personajes/player.png", false);
        spriteEnemigo = cargarTextura("personajes/esqueletoEnemigo.png", false);

        // === HUD ===
        corazon100 = cargarTextura("imagenes/corazon100%.png", false);
        corazon75 = cargarTextura("imagenes/corazon75%.png", false);
        corazon50 = cargarTextura("imagenes/corazon50%.png", false);
        corazon25 = cargarTextura("imagenes/corazon25%.png", false);
        moneda = cargarTextura("imagenes/moneda.png", false);

        // === ICONOS DE HABILIDADES ===
        iconoEspada = cargarTextura("imagenes/espada.PNG", false);
        iconoEspadaDoble = cargarTextura("imagenes/espadaDoble.PNG", false);
        iconoEspadaRoja = cargarTextura("imagenes/espadaRoja.PNG", false);
        iconoBotas = cargarTextura("imagenes/botas.png", false);
        iconoBotas2 = cargarTextura("imagenes/botas2.PNG", false);
        iconoBotasDoradas = cargarTextura("imagenes/botasDoradas.PNG", false);
        iconoCorazon = cargarTextura("imagenes/corazon.png", false);
        iconoCorazonDorado = cargarTextura("imagenes/corazonDorado.PNG", false);
        iconoEscudo = cargarTextura("imagenes/escudo.png", false);

        // === TUTORIALES ===
        tutorialMovimiento = cargarTextura("tutoriales/movimiento.png", true);
        tutorialCombate = cargarTextura("tutoriales/combate.png", true);
        tutorialOgrini = cargarTextura("Tutoriales/ogrini.jpg", true);
        tutorialArbol = cargarTextura("tutoriales/arbol.png", true);
        tutorialObjetivo = cargarTextura("Fondos/Win1.jpg", true); // Reusa fondoWin

        cargado = true;
        System.out.println("✅ ImageManager: Todas las imágenes cargadas correctamente");
    }

    /**
     * Método auxiliar para cargar texturas con filtros opcionales
     */
    private static Texture cargarTextura(String ruta, boolean aplicarFiltro) {
        Texture textura = new Texture(Gdx.files.internal(ruta));
        if (aplicarFiltro) {
            textura.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }
        return textura;
    }

    /**
     * Libera TODAS las texturas de memoria.
     * Debe llamarse al cerrar el juego (en dispose())
     */
    public static void dispose() {
        if (!cargado) return;

        System.out.println("🧹 Liberando todas las imágenes...");

        // Fondos
        disposeSeguro(fondoMenu);
        disposeSeguro(fondoArbol);
        disposeSeguro(fondoTienda);
        disposeSeguro(fondoPausa);
        disposeSeguro(fondoGameOver);
        disposeSeguro(fondoWin);

        // Personajes
        disposeSeguro(spriteJugador);
        disposeSeguro(spriteEnemigo);

        // HUD
        disposeSeguro(corazon100);
        disposeSeguro(corazon75);
        disposeSeguro(corazon50);
        disposeSeguro(corazon25);
        disposeSeguro(moneda);

        // Iconos
        disposeSeguro(iconoEspada);
        disposeSeguro(iconoEspadaDoble);
        disposeSeguro(iconoEspadaRoja);
        disposeSeguro(iconoBotas);
        disposeSeguro(iconoBotas2);
        disposeSeguro(iconoBotasDoradas);
        disposeSeguro(iconoCorazon);
        disposeSeguro(iconoCorazonDorado);
        disposeSeguro(iconoEscudo);

        // Tutoriales
        disposeSeguro(tutorialMovimiento);
        disposeSeguro(tutorialCombate);
        disposeSeguro(tutorialOgrini);
        disposeSeguro(tutorialArbol);
        // tutorialObjetivo usa fondoWin, ya liberado

        cargado = false;
        System.out.println("✅ ImageManager: Todas las imágenes liberadas");
    }

    /**
     * Método auxiliar para dispose seguro
     */
    private static void disposeSeguro(Texture textura) {
        if (textura != null) {
            textura.dispose();
        }
    }

    // =====================================================
    // GETTERS - Métodos estáticos para obtener texturas
    // =====================================================

    // === FONDOS ===
    public static Texture getFondoMenu() {
        verificarCargado();
        return fondoMenu;
    }

    public static Texture getFondoArbol() {
        verificarCargado();
        return fondoArbol;
    }

    public static Texture getFondoTienda() {
        verificarCargado();
        return fondoTienda;
    }

    public static Texture getFondoPausa() {
        verificarCargado();
        return fondoPausa;
    }

    public static Texture getFondoGameOver() {
        verificarCargado();
        return fondoGameOver;
    }

    public static Texture getFondoWin() {
        verificarCargado();
        return fondoWin;
    }

    // === PERSONAJES ===
    public static Texture getSpriteJugador() {
        verificarCargado();
        return spriteJugador;
    }

    public static Texture getSpriteEnemigo() {
        verificarCargado();
        return spriteEnemigo;
    }

    // === HUD ===
    public static Texture getCorazon100() {
        verificarCargado();
        return corazon100;
    }

    public static Texture getCorazon75() {
        verificarCargado();
        return corazon75;
    }

    public static Texture getCorazon50() {
        verificarCargado();
        return corazon50;
    }

    public static Texture getCorazon25() {
        verificarCargado();
        return corazon25;
    }

    public static Texture getMoneda() {
        verificarCargado();
        return moneda;
    }

    // === ICONOS HABILIDADES ===
    public static Texture getIconoEspada() {
        verificarCargado();
        return iconoEspada;
    }

    public static Texture getIconoEspadaDoble() {
        verificarCargado();
        return iconoEspadaDoble;
    }

    public static Texture getIconoEspadaRoja() {
        verificarCargado();
        return iconoEspadaRoja;
    }

    public static Texture getIconoBotas() {
        verificarCargado();
        return iconoBotas;
    }

    public static Texture getIconoBotas2() {
        verificarCargado();
        return iconoBotas2;
    }

    public static Texture getIconoBotasDoradas() {
        verificarCargado();
        return iconoBotasDoradas;
    }

    public static Texture getIconoCorazon() {
        verificarCargado();
        return iconoCorazon;
    }

    public static Texture getIconoCorazonDorado() {
        verificarCargado();
        return iconoCorazonDorado;
    }

    public static Texture getIconoEscudo() {
        verificarCargado();
        return iconoEscudo;
    }

    // === TUTORIALES ===
    public static Texture getTutorialMovimiento() {
        verificarCargado();
        return tutorialMovimiento;
    }

    public static Texture getTutorialCombate() {
        verificarCargado();
        return tutorialCombate;
    }

    public static Texture getTutorialOgrini() {
        verificarCargado();
        return tutorialOgrini;
    }

    public static Texture getTutorialArbol() {
        verificarCargado();
        return tutorialArbol;
    }

    public static Texture getTutorialObjetivo() {
        verificarCargado();
        return tutorialObjetivo;
    }

    /**
     * Verifica que las imágenes estén cargadas antes de usarlas
     */
    private static void verificarCargado() {
        if (!cargado) {
            throw new IllegalStateException(
                "❌ ERROR: ImageManager.cargar() debe llamarse antes de usar las imágenes"
            );
        }
    }

    /**
     * Verifica si las imágenes están cargadas
     */
    public static boolean estaCargado() {
        return cargado;
    }
}
