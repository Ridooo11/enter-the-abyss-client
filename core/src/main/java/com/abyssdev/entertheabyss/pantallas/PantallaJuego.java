package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.interfaces.GameController;
import com.abyssdev.entertheabyss.mapas.Mapa;
import com.abyssdev.entertheabyss.mapas.Sala;
import com.abyssdev.entertheabyss.mapas.SpawnPoint;
import com.abyssdev.entertheabyss.mapas.ZonaTransicion;
import com.abyssdev.entertheabyss.logica.ManejoEntradas;
import com.abyssdev.entertheabyss.network.ClientThread;
import com.abyssdev.entertheabyss.personajes.Boss;
import com.abyssdev.entertheabyss.personajes.Enemigo;
import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.personajes.Accion;
import com.abyssdev.entertheabyss.personajes.Direccion;
import com.abyssdev.entertheabyss.ui.Hud;
import com.abyssdev.entertheabyss.ui.Sonidos;
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

public class PantallaJuego extends Pantalla implements GameController {

    private OrthographicCamera camara;
    private Viewport viewport;

    // 🎮 Mapa de jugadores (para multijugador)
    private HashMap<Integer, Jugador> jugadores = new HashMap<>();
    private Jugador jugadorLocal; // El jugador de este cliente
    private int miNumeroJugador = -1;

    private Mapa mapaActual;
    private Sala salaActual;
    private ManejoEntradas inputProcessor;
    private boolean jugadorCercaDeOgrini = false;
    private static final float DISTANCIA_INTERACCION = 3f;

    // Transicion
    private boolean enTransicion = false;
    private boolean faseSubida = true;
    private float fadeAlpha = 0f;
    private float fadeSpeed = 2f;
    private String salaDestinoId = null;
    private Texture texturaFade;

    // HUD
    private Hud hud;
    private boolean yaInicializado = false;

    // 🌐 RED
    private ClientThread clientThread;
    private boolean juegoIniciado = false;
    private boolean conectado = false;

    // UI
    private BitmapFont font;
    private String mensajeEspera = "Esperando al otro jugador...";

    public PantallaJuego(Game juego, SpriteBatch batch) {
        super(juego, batch);
    }

    @Override
    public void show() {
        if (!yaInicializado) {
            // Inicializar jugador local (todavía no sabemos su número)
            jugadorLocal = new Jugador();

            // Inicializar mapa
            mapaActual = new Mapa("mazmorra1");
            mapaActual.agregarSala(new Sala("sala1", "maps/mapa1_sala1.tmx", 5));
            mapaActual.agregarSala(new Sala("sala2", "maps/mapa1_sala2.tmx", 1));
            mapaActual.agregarSala(new Sala("sala5", "maps/mapa2_posible.tmx", 15));
            mapaActual.agregarSala(new Sala("sala4", "maps/mapa1_sala4.tmx", 1));
            mapaActual.agregarSala(new Sala("sala3", "maps/mapa1_sala5.tmx", 1));

            camara = new OrthographicCamera();
            viewport = new FitViewport(32, 32 * (9f / 16f), camara);
            texturaFade = generarTextura();
            cambiarSala("sala1");
            hud = new Hud(jugadorLocal, viewport);
            inputProcessor = new ManejoEntradas(jugadorLocal);

            // Font para mensajes
            font = new BitmapFont();
            font.getData().setScale(2f);
            font.setColor(Color.WHITE);

            // 🌐 Conectar al servidor
            clientThread = new ClientThread(this);
            clientThread.start();

            // ⚠️ Dar tiempo al servidor para estar listo
            try {
                Thread.sleep(500); // Esperar 500ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            clientThread.sendMessage("Connect");

            yaInicializado = true;

            System.out.println("🎮 Cliente iniciado. Conectando al servidor...");
        } else {
            actualizarCamara();
        }
        Gdx.input.setInputProcessor(inputProcessor);
    }

    private void cambiarSala(String destinoId) {
        Sala salaDestino = mapaActual.getSala(destinoId);
        if (salaDestino == null) {
            Gdx.app.error("PantallaJuego", "Sala destino no encontrada: " + destinoId);
            return;
        }

        Sala salaAnterior = salaActual;
        salaActual = salaDestino;
        mapaActual.establecerSalaActual(destinoId);

        if (enTransicion && salaDestinoId != null) {
            for (ZonaTransicion zona : salaAnterior.getZonasTransicion()) {
                if (zona.destinoSalaId.equals(destinoId)) {
                    SpawnPoint spawn = null;
                    for (SpawnPoint sp : salaDestino.getSpawnPoints()) {
                        if (sp.name.equals(zona.spawnName) && sp.salaId.equals(destinoId)) {
                            spawn = sp;
                            break;
                        }
                    }

                    if (spawn != null) {
                        jugadorLocal.setX(spawn.x);
                        jugadorLocal.setY(spawn.y);
                    } else {
                        if (!salaDestino.getSpawnPoints().isEmpty()) {
                            SpawnPoint fallback = salaDestino.getSpawnPoints().first();
                            jugadorLocal.setX(fallback.x);
                            jugadorLocal.setY(fallback.y);
                        } else {
                            centrarJugadorEnSala();
                        }
                    }
                    break;
                }
            }
        } else {
            SpawnPoint defaultSpawn = null;
            for (SpawnPoint sp : salaDestino.getSpawnPoints()) {
                if (sp.name.equals("default") && sp.salaId.equals(destinoId)) {
                    defaultSpawn = sp;
                    break;
                }
            }
            if (defaultSpawn != null) {
                jugadorLocal.setX(defaultSpawn.x);
                jugadorLocal.setY(defaultSpawn.y);
            } else {
                centrarJugadorEnSala();
            }
        }

        camara.position.set(jugadorLocal.getX(), jugadorLocal.getY(), 0);
        camara.update();
        salaActual.getRenderer().setView(camara);

        // 🔹 NO generar enemigos en el cliente
        // if (salaActual.getEnemigos() == null || salaActual.getEnemigos().isEmpty()) {
        //     salaActual.generarEnemigos();
        // }
    }

    private void centrarJugadorEnSala() {
        float centroX = salaActual.getAnchoMundo() / 2f;
        float centroY = salaActual.getAltoMundo() / 2f;
        jugadorLocal.setX(centroX);
        jugadorLocal.setY(centroY);
    }

    private void verificarTransiciones() {
        if (enTransicion) return;

        Rectangle hitboxJugador = new Rectangle(
            jugadorLocal.getX() + jugadorLocal.getAncho() / 4f,
            jugadorLocal.getY(),
            jugadorLocal.getAncho() / 2f,
            jugadorLocal.getAlto()
        );

        for (ZonaTransicion zona : salaActual.getZonasTransicion()) {
            if (hitboxJugador.overlaps(zona)) {
                if (salaActual.hayEnemigosVivos()) {
                    System.out.println("No se ha matado a todos los enemigos");
                    return;
                }
                String destinoId = zona.destinoSalaId;
                Sala salaDestino = mapaActual.getSala(destinoId);

                if (salaDestino != null) {
                    enTransicion = true;
                    faseSubida = true;
                    salaDestinoId = destinoId;
                    fadeAlpha = 0f;

                    // 🌐 Notificar cambio de sala
                    clientThread.sendMessage("ChangeRoom:" + destinoId);
                    break;
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Pantalla de espera antes de conectar
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
                clientThread.terminate();
                juego.setScreen(new MenuInicio(juego, batch));
            }
            return;
        }

        salaActual.getRenderer().setView(camara);
        salaActual.getRenderer().render();

        // ⚠️ Los enemigos se manejan por el servidor, solo los dibujamos
        ArrayList<Enemigo> enemigos = salaActual.getEnemigos();

        // Verificar colisiones con enemigos (solo visual en cliente)
        if (enemigos != null) {
            for (int i = enemigos.size() - 1; i >= 0; i--) {
                Enemigo enemigo = enemigos.get(i);

                // Si el jugador local ataca y golpea al enemigo
                if (jugadorLocal.getHitboxAtaque().getWidth() > 0) {
                    if (!enemigo.debeEliminarse() && jugadorLocal.getHitboxAtaque().overlaps(enemigo.getRectangulo())) {
                        enemigo.recibirDanio(jugadorLocal.getDanio());

                        // Si el enemigo muere, notificar al servidor
                        if (enemigo.debeEliminarse()) {
                            clientThread.sendMessage("EnemyKilled:" + i);
                        }
                    }
                }
            }
        }

        // Boss logic
        if (salaActual.getId().equalsIgnoreCase("sala5")) {
            Boss boss = salaActual.getBoss();
            if (boss != null && !boss.debeEliminarse()) {
                // Si el jugador local ataca al boss
                if (jugadorLocal.getHitboxAtaque().getWidth() > 0) {
                    if (jugadorLocal.getHitboxAtaque().overlaps(boss.getRectangulo())) {
                        boss.recibirDanio(jugadorLocal.getDanio());

                        // Si el boss muere, notificar al servidor
                        if (boss.debeEliminarse()) {
                            clientThread.sendMessage("BossKilled");
                        }
                    }
                }
            }
        }

        try {
            salaActual.actualizarPuertas();
        } catch (Exception e) {
            System.out.println("ERROR AL ACTUALIZAR PUERTAS");
            e.printStackTrace();
        }

        // Actualizar jugador local
        float antesX = jugadorLocal.getX();
        float antesY = jugadorLocal.getY();

        jugadorLocal.update(delta, salaActual.getColisiones());

        // 🔹 Si el jugador se movió, enviar posición Y animación al servidor
        if (antesX != jugadorLocal.getX() || antesY != jugadorLocal.getY()) {
            String action = jugadorLocal.getAccionActual().name();
            String direction = jugadorLocal.getDireccionActual().name();
            clientThread.sendMessage("Move:" + jugadorLocal.getX() + ":" + jugadorLocal.getY() +
                ":" + action + ":" + direction);
        }

        // Si el jugador atacó, notificar al servidor
        if (jugadorLocal.getHitboxAtaque().getWidth() > 0) {
            clientThread.sendMessage("Attack");
        }

        verificarProximidadOgrini();
        verificarTransiciones();

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
        }

        if (jugadorCercaDeOgrini && Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            Sonidos.pausarMusicaJuego();
            juego.setScreen(new PantallaTienda(juego, batch, jugadorLocal, this));
        }

        actualizarCamara();

        batch.setProjectionMatrix(camara.combined);
        batch.begin();

        // Dibujar enemigos
        if (enemigos != null) {
            for (Enemigo enemigo : enemigos) {
                enemigo.renderizar(batch);
            }
        }

        // Dibujar boss
        Boss boss = salaActual.getBoss();
        if (boss != null) {
            boss.renderizar(batch);
        }

        // Dibujar todos los jugadores
        for (Jugador jugador : jugadores.values()) {
            jugador.dibujar(batch);
        }

        batch.end();

        if (hud != null) {
            hud.draw(batch);
        }

        if (fadeAlpha > 0f) {
            batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.begin();
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(texturaFade, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }

        if (!enTransicion) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                juego.setScreen(new PantallaPausa(juego, batch, this));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                Sonidos.pausarMusicaJuego();
                juego.setScreen(new PantallaArbolHabilidades(juego, batch, this, jugadorLocal, jugadorLocal.getHabilidades()));
            }
        }
    }

    private void verificarProximidadOgrini() {
        jugadorCercaDeOgrini = false;
        if (salaActual == null || salaActual.getMapa() == null) return;

        com.badlogic.gdx.maps.MapLayer capaObjetos = salaActual.getMapa().getLayers().get("colisiones");
        if (capaObjetos == null) return;

        com.badlogic.gdx.maps.MapObjects objetos = capaObjetos.getObjects();
        for (com.badlogic.gdx.maps.MapObject objeto : objetos) {
            if (!(objeto instanceof com.badlogic.gdx.maps.objects.RectangleMapObject)) continue;

            String nombre = objeto.getProperties().get("nombre", String.class);
            String tipo = objeto.getProperties().get("tipo", String.class);

            if (nombre != null && nombre.equalsIgnoreCase("ogrini") &&
                tipo != null && tipo.equalsIgnoreCase("tienda")) {

                com.badlogic.gdx.maps.objects.RectangleMapObject rectObj =
                    (com.badlogic.gdx.maps.objects.RectangleMapObject) objeto;
                com.badlogic.gdx.math.Rectangle rect = rectObj.getRectangle();

                float objX = (rect.x + rect.width / 2f) / 16f;
                float objY = (rect.y + rect.height / 2f) / 16f;

                float distancia = (float) Math.sqrt(
                    Math.pow(jugadorLocal.getX() - objX, 2) +
                        Math.pow(jugadorLocal.getY() - objY, 2)
                );

                if (distancia <= DISTANCIA_INTERACCION) {
                    jugadorCercaDeOgrini = true;
                    break;
                }
            }
        }
    }

    private void actualizarCamara() {
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

    public Texture generarTextura() {
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

        // Agregar mi jugador al mapa de jugadores
        jugadores.put(numPlayer, jugadorLocal);
    }

    @Override
    public void start() {
        System.out.println("🎮 ¡Juego iniciado!");

        // ✅ SOLUCIÓN: Ejecutar en el thread de renderizado
        final int otroJugador = (miNumeroJugador == 1) ? 2 : 1;

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                juegoIniciado = true;

                // Crear el otro jugador
                Jugador jugador2 = new Jugador();
                jugador2.setX(jugadorLocal.getX() + 2);
                jugador2.setY(jugadorLocal.getY());
                jugadores.put(otroJugador, jugador2);

                System.out.println("✅ Jugador " + otroJugador + " creado correctamente");
            }
        });
    }

    @Override
    public void updatePlayerPosition(int numPlayer, float x, float y) {
        // No actualizar mi propia posición
        if (numPlayer == miNumeroJugador) return;

        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            jugador.setX(x);
            jugador.setY(y);
        }
    }

    // 🔹 NUEVO: Actualizar animación del otro jugador
    @Override
    public void updatePlayerAnimation(int numPlayer, String action, String direction) {
        if (numPlayer == miNumeroJugador) return;

        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            try {
                Accion accion = Accion.valueOf(action);
                Direccion dir = Direccion.valueOf(direction);
                jugador.setAccionActual(accion);
                jugador.setDireccionActual(dir);
            } catch (IllegalArgumentException e) {
                System.err.println("⚠️ Animación inválida: " + action + ", " + direction);
            }
        }
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
        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            jugador.setMonedas(coins);
            System.out.println("💰 Jugador " + numPlayer + " ahora tiene " + coins + " monedas");
        }
    }

    @Override
    public void updateHealth(int numPlayer, int health) {
        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            jugador.setVida(health);
            System.out.println("❤️ Jugador " + numPlayer + " ahora tiene " + health + " de vida");
        }
    }

    @Override
    public void updateRoomChange(String roomId) {
        System.out.println("🚪 Cambiando a sala: " + roomId);

        final String finalRoomId = roomId;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                cambiarSala(finalRoomId);
            }
        });
    }

    @Override
    public void playerAttack(int numPlayer) {
        System.out.println("⚔️ Jugador " + numPlayer + " atacó");
        // Aquí podrías reproducir una animación de ataque del otro jugador
    }

    @Override
    public void endGame(int winner) {
        System.out.println("🏆 Jugador " + winner + " ganó el juego");

        final int finalWinner = winner;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Sonidos.detenerTodaMusica();

                if (finalWinner == miNumeroJugador) {
                    juego.setScreen(new PantallaWin(juego, batch));
                } else {
                    juego.setScreen(new PantallaGameOver(juego, batch));
                }
            }
        });
    }

    @Override
    public void backToMenu() {
        System.out.println("🔙 Volviendo al menú");

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                clientThread.terminate();
                juego.setScreen(new MenuInicio(juego, batch));
            }
        });
    }

    // 🔹 NUEVO: Sincronizar enemigos desde el servidor
    @Override
    public void syncEnemies(final String enemiesData) {
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
                            enemigos.add(new Enemigo(x, y, 3f, 2f, 10));
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

    // ========================================
    // 🧹 LIMPIEZA
    // ========================================

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        actualizarCamara();
    }

    @Override
    public void hide() {
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
    }
}
