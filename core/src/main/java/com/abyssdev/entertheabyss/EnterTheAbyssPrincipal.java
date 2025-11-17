package com.abyssdev.entertheabyss;

import com.abyssdev.entertheabyss.pantallas.MenuInicio;
import com.abyssdev.entertheabyss.pantallas.PantallaJuego;
import com.abyssdev.entertheabyss.pantallas.PantallaWin;
import com.abyssdev.entertheabyss.ui.Imagenes;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EnterTheAbyssPrincipal extends Game {
    public SpriteBatch batch; // SpriteBatch usado por todas las pantallas que va a tener el juego
    private Preferences prefs;
    private Thread shutdownHook;

    @Override
    public void create() {
        batch = new SpriteBatch();


        // ✅ CARGAR TODAS LAS IMÁGENES UNA SOLA VEZ
        Imagenes.cargar();
        // Inicializar preferencias
        prefs = Gdx.app.getPreferences("EnterTheAbyss_Settings");

        float volumenMusica = prefs.getFloat("volumenMusica", .2f);
        float volumenEfectos = prefs.getFloat("volumenEfectos", .2f);

        // Inicializar sonidos y aplicar volúmenes
        Sonidos.cargar();
        Sonidos.setVolumenMusica(volumenMusica);
        Sonidos.setVolumenEfectos(volumenEfectos);

        // Arrancar en el menú
        setScreen(new MenuInicio(this,batch));

        shutdownHook = new Thread(() -> {
            System.out.println("🛑 Ventana cerrada detectada - Desconectando...");

            // Desconectar si es PantallaJuego
            if (getScreen() instanceof PantallaJuego) {
                PantallaJuego pantallaJuego = (PantallaJuego) getScreen();

                // Forzar desconexión inmediata
                if (pantallaJuego.getClientThread() != null) {
                    try {
                        pantallaJuego.getClientThread().sendMessage("Disconnect");
                        Thread.sleep(200); // Esperar a que el mensaje se envíe
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("✅ Desconexión completada");
        });

        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        batch.dispose();
        Sonidos.dispose();
        Imagenes.dispose(); // ✅ LIBERAR IMÁGENES

        try {
            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
        } catch (IllegalStateException e) {
            // Ya se está ejecutando el shutdown
        }
    }


    public Preferences getPreferencias() {
        return prefs;
    }
}
