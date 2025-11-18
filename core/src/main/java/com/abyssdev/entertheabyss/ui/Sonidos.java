package com.abyssdev.entertheabyss.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Sonidos {

    // ✅ Música
    private static Music musicaMenu;
    //private static Music musicaJuego;
    private static Music musicaDerrota; // ✅ Nueva

    // ✅ Efectos
    private static Sound sonidoAtaque;
    private static Sound compraExitosa;
    private static Sound compraFallida;
    private static Sound sonidoEvasion;
    private static Sound sonidoPuerta;
    private static Sound sonidoVictoria;

    public static void cargar() {
        // Música
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/menu.mp3"));
        //musicaJuego = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/juego.mp3"));
        musicaDerrota = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/game_over.mp3")); // ✅ Cargar

        // Configurar música
        musicaMenu.setLooping(true);
        //musicaJuego.setLooping(true);
        musicaDerrota.setLooping(false); // ✅ No repetir en Game Over

        // Efectos
        sonidoAtaque = Gdx.audio.newSound(Gdx.files.internal("sonidos/efectos/espada.mp3"));
        compraExitosa = Gdx.audio.newSound(Gdx.files.internal("sonidos/efectos/compra_exitosa.mp3"));
        compraFallida = Gdx.audio.newSound(Gdx.files.internal("sonidos/efectos/compra_fallida.mp3"));
        sonidoEvasion = Gdx.audio.newSound(Gdx.files.internal("sonidos/efectos/dash.mp3"));
        sonidoPuerta = Gdx.audio.newSound(Gdx.files.internal("sonidos/efectos/puerta.mp3"));
        sonidoVictoria = Gdx.audio.newSound(Gdx.files.internal("sonidos/efectos/victoria.mp3"));
    }

    // ✅ Efectos
    public static void reproducirAtaque() {
        if (sonidoAtaque != null) {
            sonidoAtaque.play(volumenEfectos);
        }
    }

    public static void reproducirCompraExitosa() {
        compraExitosa.play(volumenEfectos);
    }

    public static void reproducirCompraFallida() {
        compraFallida.play(volumenEfectos);
    }

    public static void reproducirEvasion() {
        if (sonidoEvasion != null) sonidoEvasion.play(volumenEfectos);
    }

    public static void reproducirSonidoPuerta() {
        if (sonidoPuerta != null) sonidoPuerta.play(volumenEfectos);
    }

    public static void reproducirSonidoVictoria() {
        if (sonidoVictoria != null) sonidoVictoria.play(volumenEfectos);
    }

    private static float volumenMusica = 0.2f;
    private static float volumenEfectos = 0.2f;

    // ✅ AGREGAR ESTOS MÉTODOS
    public static void setVolumenMusica(float volumen) {
        volumenMusica = Math.max(0f, Math.min(1f, volumen));
        if (musicaMenu != null) musicaMenu.setVolume(volumenMusica);
        //if (musicaJuego != null) musicaJuego.setVolume(volumenMusica);
        if (musicaDerrota != null) musicaDerrota.setVolume(volumenMusica);
    }

    public static void setVolumenEfectos(float volumen) {
        volumenEfectos = Math.max(0f, Math.min(1f, volumen));
    }

    public static float getVolumenMusica() {
        return volumenMusica;
    }

    public static float getVolumenEfectos() {
        return volumenEfectos;
    }

    public static void reproducirMusicaMenu() {
        detenerTodaMusica();

        // ✅ NUEVO: Recrear si está disposed
        try {
            if (musicaMenu == null || !musicaMenu.isPlaying()) {
                musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/menu.mp3"));
                musicaMenu.setLooping(true);
            }
            musicaMenu.setVolume(volumenMusica);
            musicaMenu.play();
        } catch (Exception e) {
            System.err.println("⚠️ Error reproduciendo música menú: " + e.getMessage());
            // Intentar recargar
            try {
                musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/menu.mp3"));
                musicaMenu.setLooping(true);
                musicaMenu.setVolume(volumenMusica);
                musicaMenu.play();
            } catch (Exception e2) {
                System.err.println("❌ No se pudo recuperar música menú");
            }
        }
    }

   public static void reproducirMusicaJuego() {
        detenerTodaMusica();

        /*//
        try {
            if (musicaJuego == null || !musicaJuego.isPlaying()) {
                musicaJuego = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/juego.mp3"));
                musicaJuego.setLooping(true);
            }
            musicaJuego.setVolume(volumenMusica);
            musicaJuego.play();
        } catch (Exception e) {
            System.err.println("⚠️ Error reproduciendo música juego: " + e.getMessage());
            // Intentar recargar
            try {
                musicaJuego = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/juego.mp3"));
                musicaJuego.setLooping(true);
                musicaJuego.setVolume(volumenMusica);
                musicaJuego.play();
            } catch (Exception e2) {
                System.err.println("❌ No se pudo recuperar música juego");
            }
        }*/
    }

    public static void reproducirMusicaDerrota() {
        detenerTodaMusica();

        // ✅ NUEVO: Recrear si está disposed
        try {
            if (musicaDerrota == null) {
                musicaDerrota = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/game_over.mp3"));
                musicaDerrota.setLooping(false);
            }
            musicaDerrota.setVolume(volumenMusica);
            musicaDerrota.play();
        } catch (Exception e) {
            System.err.println("⚠️ Error reproduciendo música derrota: " + e.getMessage());
            try {
                musicaDerrota = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica/game_over.mp3"));
                musicaDerrota.setLooping(false);
                musicaDerrota.setVolume(volumenMusica);
                musicaDerrota.play();
            } catch (Exception e2) {
                System.err.println("❌ No se pudo recuperar música derrota");
            }
        }
    }

    public static void pausarMusicaJuego() {
        /*if (musicaJuego != null && musicaJuego.isPlaying()) {
            musicaJuego.pause();
        }*/
    }

    public static void reanudarMusicaJuego() {
        /*if (musicaJuego != null && !musicaJuego.isPlaying()) {
            musicaJuego.play();
        }*/
    }

    public static void detenerTodaMusica() {
        try {
            if (musicaMenu != null && musicaMenu.isPlaying()) {
                musicaMenu.stop();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error deteniendo música menú");
        }

        /*try {
            if (musicaJuego != null && musicaJuego.isPlaying()) {
                musicaJuego.stop();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error deteniendo música juego");
        }*/

        try {
            if (musicaDerrota != null && musicaDerrota.isPlaying()) {
                musicaDerrota.stop();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error deteniendo música derrota");
        }
    }

    public static void dispose() {

    }

    public static void disposeCompletamente() {
        System.out.println("🧹 Liberando TODOS los sonidos...");

        detenerTodaMusica();

        if (musicaMenu != null) {
            musicaMenu.dispose();
            musicaMenu = null;
        }
        /*if (musicaJuego != null) {
            musicaJuego.dispose();
            musicaJuego = null;
        }*/
        if (musicaDerrota != null) {
            musicaDerrota.dispose();
            musicaDerrota = null;
        }
        if (sonidoAtaque != null) {
            sonidoAtaque.dispose();
            sonidoAtaque = null;
        }
        if (sonidoPuerta != null) {
            sonidoPuerta.dispose();
            sonidoPuerta = null;
        }
        if (compraExitosa != null) {
            compraExitosa.dispose();
            compraExitosa = null;
        }
        if (compraFallida != null) {
            compraFallida.dispose();
            compraFallida = null;
        }
        if (sonidoEvasion != null) {
            sonidoEvasion.dispose();
            sonidoEvasion = null;
        }
        if (sonidoVictoria != null) {
            sonidoVictoria.dispose();
            sonidoVictoria = null;
        }
    }
}
