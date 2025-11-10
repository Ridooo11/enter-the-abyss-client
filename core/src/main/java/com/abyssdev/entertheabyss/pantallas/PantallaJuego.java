package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.logica.ManejoEntradas;
import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.network.ClientThread;
import com.abyssdev.entertheabyss.interfaces.GameController;
import com.abyssdev.entertheabyss.mapas.*;
import com.abyssdev.entertheabyss.personajes.*;
import com.abyssdev.entertheabyss.pantallas.MenuInicio;
import com.abyssdev.entertheabyss.pantallas.Pantalla;
import com.abyssdev.entertheabyss.ui.Hud;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * PantallaJuegoCliente - Versión CLIENTE
 *
 * RESPONSABILIDADES:
 * - Capturar inputs del jugador local
 * - Enviar inputs al servidor
 * - Renderizar el estado recibido del servidor
 * - NO calcular lógica de juego
 */
public class PantallaJuego extends Pantalla implements GameController {

    private OrthographicCamera camara;
    private Viewport viewport;

    private ArrayList<Rectangle> zonasTransicion = new ArrayList<>();
    private HashMap<Rectangle, ZonaTransicion> mapaZonas = new HashMap<>();

    // 🌐 Red
    private ClientThread clientThread;
    private boolean conectado = false;
    private boolean juegoIniciado = false;

    // 🎮 Jugadores (solo visuales)
    private HashMap<Integer, Jugador> jugadores = new HashMap<>();
    private Jugador jugadorLocal;
    private int miNumeroJugador = -1;

    // 🗺️ Mundo (solo para renderizado)
    private Mapa mapaActual;
    private Sala salaActual;

    // 🎨 Renderizado
    private Texture texturaFade;
    private boolean enTransicion = false;
    private float fadeAlpha = 0f;
    private float fadeSpeed = 2f;
    private boolean faseSubida = true;
    private String salaDestinoId = null;

    // 📊 HUD
    private Hud hud;
    private int vidaLocal = 100;
    private int vidaMaximaLocal = 100;
    private int monedasLocal = 0;

    // ⌨️ Estado de inputs (para enviar al servidor)
    private boolean arriba, abajo, izquierda, derecha;
    private boolean inputsEnviados = false;

    // 📝 UI
    private BitmapFont font;
    private String mensajeEspera = "Conectando al servidor...";

    public PantallaJuego(Game juego, SpriteBatch batch) {
        super(juego, batch);
    }

    @Override
    public void show() {
        System.out.println("🎮 CLIENTE INICIADO");

        // Inicializar cámara y viewport
        camara = new OrthographicCamera();
        viewport = new FitViewport(32, 32 * (9f / 16f), camara);
        texturaFade = generarTextura();

        // Font para mensajes
        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);

        // Inicializar mapa (solo para renderizado)
        mapaActual = new Mapa("mazmorra1");
        mapaActual.agregarSala(new Sala("sala1", "maps/mapa1_sala1.tmx"));
        mapaActual.agregarSala(new Sala("sala2", "maps/mapa1_sala2.tmx"));
        mapaActual.agregarSala(new Sala("sala5", "maps/mapa2_posible.tmx"));
        mapaActual.agregarSala(new Sala("sala4", "maps/mapa1_sala4.tmx"));
        mapaActual.agregarSala(new Sala("sala3", "maps/mapa1_sala5.tmx"));

        salaActual = mapaActual.getSala("sala1");


        // Conectar al servidor
        clientThread = new ClientThread(this);
        clientThread.start();

        // Esperar un momento antes de conectar
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clientThread.sendMessage("Connect");
        System.out.println("📡 Conectando al servidor...");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Pantalla de espera
        if (!conectado || !juegoIniciado) {
            batch.begin();
            font.draw(batch, mensajeEspera,
                Gdx.graphics.getWidth() / 2f - 200,
                Gdx.graphics.getHeight() / 2f);

            font.draw(batch, "Presiona ESC para volver al menú",
                Gdx.graphics.getWidth() / 2f - 250,
                Gdx.graphics.getHeight() / 2f - 50);
            batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                backToMenu();
            }
            return;
        }


        // 🎨 RENDERIZAR
        salaActual.getRenderer().setView(camara);
        salaActual.getRenderer().render();

        // Actualizar animaciones de jugadores
        for (Jugador jugador : jugadores.values()) {
            jugador.update(delta);
        }

        actualizarCamara();

        batch.setProjectionMatrix(camara.combined);
        batch.begin();

        // Dibujar enemigos (si los hay)
        ArrayList<Enemigo> enemigos = salaActual.getEnemigos();
        if (enemigos != null) {
            for (Enemigo enemigo : enemigos) {
                enemigo.update(delta);
                enemigo.renderizar(batch);

            }
        }

        // Dibujar boss
        Boss boss = salaActual.getBoss();
        if (boss != null) {
            boss.renderizar(batch);
        }

        // Dibujar jugadores
        for (Jugador jugador : jugadores.values()) {
            jugador.dibujar(batch);
        }

        batch.end();

        // Dibujar HUD
        if (hud != null) {
            hud.draw(batch);
        }




        // Efecto de fade
        if (enTransicion) {
            if (faseSubida) {
                fadeAlpha += fadeSpeed * delta;
                if (fadeAlpha >= 1f) {
                    fadeAlpha = 1f;
                    cambiarSala(salaDestinoId);
                    faseSubida = false;
                }
            } else {
                fadeAlpha -= fadeSpeed * delta;
                if (fadeAlpha <= 0f) {
                    fadeAlpha = 0f;
                    enTransicion = false;
                    salaDestinoId = null;
                }
            }

            batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.begin();
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(texturaFade, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            juego.setScreen(new PantallaPausa(juego, batch, this));
        }
    }

    private void cambiarSala(String destinoId) {
        Sala salaDestino = mapaActual.getSala(destinoId);
        if (salaDestino == null) {
            System.err.println("❌ Sala destino no encontrada en cliente: " + destinoId);
            return;
        }

        salaActual = salaDestino;
        mapaActual.establecerSalaActual(destinoId);

        // 🔹 Buscar spawn correspondiente
        SpawnPoint spawn = null;
        for (SpawnPoint sp : salaDestino.getSpawnPoints()) {
            if (sp.name.equals("spawn_centro") && sp.salaId.equals(destinoId)) {
                spawn = sp;
                break;
            }
        }

        if (jugadorLocal != null) {
            if (spawn != null) {
                jugadorLocal.setX(spawn.x);
                jugadorLocal.setY(spawn.y);
            } else if (!salaDestino.getSpawnPoints().isEmpty()) {
                SpawnPoint fallback = salaDestino.getSpawnPoints().first();
                jugadorLocal.setX(fallback.x);
                jugadorLocal.setY(fallback.y);
            } else {
                jugadorLocal.setX(salaDestino.getAnchoMundo() / 2f);
                jugadorLocal.setY(salaDestino.getAltoMundo() / 2f);
            }
        }

        // 🔹 Actualizar cámara y renderer
        camara.position.set(jugadorLocal.getX(), jugadorLocal.getY(), 0);
        camara.update();
        salaActual.getRenderer().setView(camara);

        System.out.println("🚪 Cliente cambió a sala " + destinoId);
    }


    private void actualizarCamara() {
        if (jugadorLocal == null) return;

        float halfWidth = camara.viewportWidth / 2f;
        float halfHeight = camara.viewportHeight / 2f;

        float x = jugadorLocal.getX();
        float y = jugadorLocal.getY();

        float limiteIzquierdo = halfWidth;
        float limiteDerecho = Math.max(limiteIzquierdo, salaActual.getAnchoMundo() - halfWidth);
        float limiteInferior = halfHeight;
        float limiteSuperior = Math.max(limiteInferior, salaActual.getAltoMundo() - halfHeight);

        x = MathUtils.clamp(x, limiteIzquierdo, limiteDerecho);
        y = MathUtils.clamp(y, limiteInferior, limiteSuperior);

        camara.position.set(x, y, 0);
        camara.update();
    }

    private Texture generarTextura() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        return textura;
    }

    // ========================================
    // 🎮 IMPLEMENTACIÓN DE GameController
    // ========================================

    @Override
    public void connect(int numPlayer) {
        System.out.println("✅ Conectado como jugador " + numPlayer);
        this.miNumeroJugador = numPlayer;
        this.conectado = true;
        this.mensajeEspera = "Esperando al jugador " + (numPlayer == 1 ? "2" : "1") + "...";
    }

    @Override
    public void start() {
        System.out.println("🎮 ¡Juego iniciado!");

        Gdx.app.postRunnable(() -> {
            juegoIniciado = true;

            jugadorLocal = new Jugador(miNumeroJugador, 10f, 10f);
            jugadores.put(miNumeroJugador, jugadorLocal);

            int otroJugador = (miNumeroJugador == 1) ? 2 : 1;
            jugadores.put(otroJugador, new Jugador(otroJugador, 12f, 10f));

            hud = new Hud(jugadorLocal, viewport);
            cambiarSala("sala1");
            ManejoEntradas manejoEntradas = new ManejoEntradas(jugadorLocal, clientThread);


            Gdx.input.setInputProcessor(manejoEntradas);
        });
    }


    @Override
    public void updatePlayerPosition(int numPlayer, float x, float y) {
        // Actualizar posición recibida del servidor
        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            jugador.setX(x);
            jugador.setY(y);
        }
    }

    @Override
    public void updatePlayerAnimation(int numPlayer, String action, String direction) {
        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            jugador.actualizarDesdeServidor(jugador.getX(), jugador.getY(), action, direction);
        }
    }



    @Override
    public void updateEnemyPosition(int id, float x, float y) {
        Gdx.app.postRunnable(() -> {
            if (salaActual == null || salaActual.getEnemigos() == null) return;

            ArrayList<Enemigo> enemigos = salaActual.getEnemigos();
            if (id >= 0 && id < enemigos.size()) {
                Enemigo enemigo = enemigos.get(id);
                if (enemigo != null) {
                    enemigo.setX(x);
                    enemigo.setY(y);
                }
            }
        });
    }

    @Override
    public void updateEnemyAnimation(int id, String action, String direction) {
        Gdx.app.postRunnable(() -> {
            if (salaActual == null || salaActual.getEnemigos() == null) return;

            ArrayList<Enemigo> enemigos = salaActual.getEnemigos();
            if (id >= 0 && id < enemigos.size()) {
                Enemigo enemigo = enemigos.get(id);
                if (enemigo != null) {
                    enemigo.actualizarDesdeServidor(
                        enemigo.getPosicion().x,
                        enemigo.getPosicion().y,
                        action,
                        direction
                    );
                }
            }
        });
    }

    @Override
    public void updateEnemyDead(int enemyId) {
        System.out.println("💀 Enemigo " + enemyId + " eliminado");

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                ArrayList<Enemigo> enemigos = salaActual.getEnemigos();
                if (enemigos != null && enemyId >= 0 && enemyId < enemigos.size()) {
                    enemigos.remove(enemyId);
                }
            }
        });
    }

    @Override
    public void updateBossDead() {
        System.out.println("👑 Boss eliminado");

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Boss boss = salaActual.getBoss();
                if (boss != null) {
                    boss.setVida(0);
                }
            }
        });
    }

    @Override
    public void updateCoins(int numPlayer, int coins) {
        if (numPlayer == miNumeroJugador) {
            monedasLocal = coins;
            System.out.println("💰 Monedas actualizadas: " + coins);
        }
    }

    @Override
    public void updateHealth(int numPlayer, int health) {
        if (numPlayer == miNumeroJugador) {
            vidaLocal = health;
            System.out.println("❤️ Vida actualizada: " + health);
        }
    }

    @Override
    public void updateDoorOpened(String roomId) {
        System.out.println("🚪 Puerta abierta en sala: " + roomId);

        Gdx.app.postRunnable(() -> {
            Sala sala = mapaActual.getSala(roomId);
            if (sala == null) return;

            sala.actualizarPuertas();
        });
    }


    @Override
    public void updateRoomChange(String roomId) {
        System.out.println("🚪 Cambiando a sala: " + roomId);
        mapaActual.establecerSalaActual(roomId);

        final String finalRoomId = roomId;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                enTransicion = true;
                faseSubida = true;
                salaDestinoId = finalRoomId;
                fadeAlpha = 0f;
            }
        });
    }

    @Override
    public void playerAttack(int numPlayer) {
        System.out.println("⚔️ Jugador " + numPlayer + " atacó");
        // Aquí podrías reproducir una animación/sonido de ataque
    }

    @Override
    public void endGame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                clientThread.sendMessage("Disconnect");
                clientThread.terminate();
                juego.setScreen(new PantallaGameOver(juego, batch));
            }
        });
    }

    @Override
    public void backToMenu() {
        System.out.println("🔙 Volviendo al menú");

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                clientThread.sendMessage("Disconnect");
                clientThread.terminate();
                juego.setScreen(new MenuInicio(juego, batch));
            }
        });
    }

    @Override
    public void syncEnemies(String enemiesData) {
        System.out.println("📍 Sincronizando enemigos: " + enemiesData);

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (salaActual == null) return;

                ArrayList<Enemigo> enemigos = new ArrayList<>();
                String[] enemyPositions = enemiesData.split(";");

                for (String pos : enemyPositions) {
                    String[] coords = pos.split(",");
                    if (coords.length == 2) {
                        try {
                            float x = Float.parseFloat(coords[0]);
                            float y = Float.parseFloat(coords[1]);
                            enemigos.add(new Enemigo(x, y, 1f, 2f, 5));
                        } catch (NumberFormatException e) {
                            System.err.println("⚠️ Error parseando posición enemigo: " + pos);
                        }
                    }
                }

                salaActual.setEnemigos(enemigos);
                System.out.println("✅ " + enemigos.size() + " enemigos sincronizados");
            }
        });
    }

    @Override
    public void spawnEnemy(int id, float x, float y) {
        if (salaActual == null) return;

        // asegurate de que la lista exista
        if (salaActual.getEnemigos() == null) {
            salaActual.setEnemigos(new ArrayList<>());
        }

        // prevenir duplicados
        ArrayList<Enemigo> enemigos = salaActual.getEnemigos();
        while (enemigos.size() <= id) {
            enemigos.add(null);
        }

        if (enemigos.get(id) == null) {
            Enemigo enemigo = new Enemigo(x, y, 1f, 2f, 5);
            enemigos.set(id, enemigo);
            System.out.println("👾 Enemigo " + id + " creado en cliente en (" + x + ", " + y + ")");
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        actualizarCamara();
    }

    @Override
    public void dispose() {
        if (clientThread != null) {
            clientThread.terminate();
        }
        if (mapaActual != null) {
            mapaActual.dispose();
        }
        if (hud != null) {
            hud.dispose();
        }
        for (Jugador jugador : jugadores.values()) {
            jugador.dispose();
        }
        if (texturaFade != null) {
            texturaFade.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        System.out.println("🔴 Cliente desconectado");
    }

    public Sala getSalaActual() {
        return this.salaActual;
    }

    public Mapa getMapaActual() {
        return this.mapaActual;
    }
}
