package com.abyssdev.entertheabyss.personajes;

import com.abyssdev.entertheabyss.habilidades.*;
import com.abyssdev.entertheabyss.personajes.Accion;
import com.abyssdev.entertheabyss.personajes.Direccion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * Versión CLIENTE del Jugador
 * SOLO renderizado y animaciones
 * NO contiene lógica de juego
 */
public class Jugador {
    // Identificación
    private int numeroJugador;
    private int monedas = 0;

    // Solo para renderizado
    private Vector2 posicion;
    private float ancho = 3f, alto = 3f;

    // Estado (recibido del servidor)
    private Accion accionActual = Accion.ESTATICO;
    private Direccion direccionActual = Direccion.ABAJO;

    // Animaciones
    private Texture hojaSprite;
    private Animation<TextureRegion>[][] animaciones;
    private float estadoTiempo = 0;

    private static final int FRAME_WIDTH = 48;
    private static final int FRAME_HEIGHT = 48;
    private static final int FRAMES_PER_ANIMATION = 3;

    private int[][] mapaFilasAnimacion;

    // Efecto visual de evasión
    private boolean mostrandoEvasion = false;
    private float parpadeoEvasion = 0f;
    private float intervaloParpadeo = 0.05f;
    private boolean mostrarFrame = true;
    private Rectangle hitbox;

    private boolean regeneracionActiva = false;
    private float tiempoRegeneracion = 0f;
    private float intervaloRegeneracion = 1f; // Cada 1 segundo
    private int cantidadRegeneracion = 1;

    // --- DASH / EVASIÓN ---
    private boolean evasionHabilitada = false;
    private boolean estaEvadendo = false;
    private float duracionEvasion = 0.3f; // dura 0.3 segundos
    private float tiempoEvasion = 0f;
    private float cooldownEvasion = 1.5f; // tiempo antes de poder volver a usar
    private float tiempoDesdeUltimaEvasion = 0f;
    private float velocidadBase = 3.2f;

    private Map<String, Habilidad> habilidades;

    public Jugador(int numeroJugador, float x, float y) {
        this.numeroJugador = numeroJugador;
        this.posicion = new Vector2(x, y);
        this.hitbox = new Rectangle(x, y, ancho, alto);

        hojaSprite = new Texture("personajes/player.png");
        inicializarMapaFilas();
        cargarAnimaciones();

        inicializarHabilidades();
    }

    private void inicializarHabilidades() {

            habilidades = new HashMap<>();
            habilidades.put("Vida Extra", new HabilidadVida());
            habilidades.put("Fuerza", new HabilidadFuerza());
            habilidades.put("Velocidad", new HabilidadVelocidad());
            habilidades.put("Defensa", new HabilidadDefensa());
            habilidades.put("Ataque Veloz", new HabilidadAtaqueVeloz());
            habilidades.put("Velocidad II", new HabilidadVelocidad2());
            habilidades.put("Regeneración", new HabilidadRegeneracion());
            habilidades.put("Golpe Crítico", new HabilidadGolpeCritico());
            habilidades.put("Evasión", new HabilidadEvasion());
    }

    public Map<String, Habilidad> getHabilidades() {
        return habilidades;
    }

    private void inicializarMapaFilas() {
        mapaFilasAnimacion = new int[Accion.values().length][Direccion.values().length];
        mapaFilasAnimacion[Accion.ESTATICO.ordinal()][Direccion.ABAJO.ordinal()] = 0;
        mapaFilasAnimacion[Accion.ESTATICO.ordinal()][Direccion.DERECHA.ordinal()] = 1;
        mapaFilasAnimacion[Accion.ESTATICO.ordinal()][Direccion.ARRIBA.ordinal()] = 2;
        mapaFilasAnimacion[Accion.ESTATICO.ordinal()][Direccion.IZQUIERDA.ordinal()] = 1;
        mapaFilasAnimacion[Accion.CAMINAR.ordinal()][Direccion.ABAJO.ordinal()] = 3;
        mapaFilasAnimacion[Accion.CAMINAR.ordinal()][Direccion.DERECHA.ordinal()] = 4;
        mapaFilasAnimacion[Accion.CAMINAR.ordinal()][Direccion.ARRIBA.ordinal()] = 5;
        mapaFilasAnimacion[Accion.CAMINAR.ordinal()][Direccion.IZQUIERDA.ordinal()] = 4;
        mapaFilasAnimacion[Accion.ATAQUE.ordinal()][Direccion.ABAJO.ordinal()] = 6;
        mapaFilasAnimacion[Accion.ATAQUE.ordinal()][Direccion.DERECHA.ordinal()] = 7;
        mapaFilasAnimacion[Accion.ATAQUE.ordinal()][Direccion.IZQUIERDA.ordinal()] = 7;
        mapaFilasAnimacion[Accion.ATAQUE.ordinal()][Direccion.ARRIBA.ordinal()] = 8;
        for (int dir = 0; dir < Direccion.values().length; dir++) {
            mapaFilasAnimacion[Accion.MUERTE.ordinal()][dir] = 9;
        }
    }

    private void cargarAnimaciones() {
        TextureRegion[][] regiones = TextureRegion.split(hojaSprite, FRAME_WIDTH, FRAME_HEIGHT);
        animaciones = new Animation[Accion.values().length][Direccion.values().length];

        for (Accion accion : Accion.values()) {
            for (Direccion dir : Direccion.values()) {
                int filaSpriteSheet = mapaFilasAnimacion[accion.ordinal()][dir.ordinal()];
                if (filaSpriteSheet >= regiones.length) {
                    animaciones[accion.ordinal()][dir.ordinal()] = new Animation<>(0.1f, new TextureRegion[1]);
                    continue;
                }

                TextureRegion[] frames = new TextureRegion[FRAMES_PER_ANIMATION];
                for (int i = 0; i < Math.min(FRAMES_PER_ANIMATION, regiones[filaSpriteSheet].length); i++) {
                    frames[i] = regiones[filaSpriteSheet][i];
                }

                float frameDuration = 0.2f;
                Animation.PlayMode playMode = Animation.PlayMode.LOOP;

                switch (accion) {
                    case ESTATICO:
                        frameDuration = 0.2f;
                        playMode = frames.length > 1 ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL;
                        break;
                    case CAMINAR:
                        frameDuration = 0.15f;
                        playMode = Animation.PlayMode.LOOP;
                        break;
                    case ATAQUE:
                        frameDuration = 0.1f;
                        playMode = Animation.PlayMode.NORMAL;
                        break;
                    case MUERTE:
                        frameDuration = 0.15f;
                        playMode = Animation.PlayMode.NORMAL;
                        break;
                }

                animaciones[accion.ordinal()][dir.ordinal()] = new Animation<>(frameDuration, frames);
                animaciones[accion.ordinal()][dir.ordinal()].setPlayMode(playMode);
            }
        }
    }

    /**
     * Actualiza solo las animaciones
     * NO actualiza física ni lógica
     */
    public void update(float delta) {
        estadoTiempo += delta;

        // Efecto visual de evasión
        if (mostrandoEvasion) {
            parpadeoEvasion += delta;
            if (parpadeoEvasion >= intervaloParpadeo) {
                mostrarFrame = !mostrarFrame;
                parpadeoEvasion = 0f;
            }
        }
    }

    /**
     * Renderiza el jugador
     */
    public void dibujar(SpriteBatch batch) {
        Animation<TextureRegion> currentAnimation = animaciones[accionActual.ordinal()][direccionActual.ordinal()];
        TextureRegion frameADibujar = currentAnimation.getKeyFrame(estadoTiempo);

        boolean voltearX = (direccionActual == Direccion.IZQUIERDA);

        TextureRegion frameParaDibujar = new TextureRegion(frameADibujar);
        if (frameParaDibujar.isFlipX() && !voltearX) {
            frameParaDibujar.flip(true, false);
        } else if (!frameParaDibujar.isFlipX() && voltearX) {
            frameParaDibujar.flip(true, false);
        }

        // Efecto visual de evasión
        if (mostrandoEvasion) {
            if (mostrarFrame) {
                batch.setColor(1f, 1f, 1f, 0.5f);
                batch.draw(frameParaDibujar, posicion.x, posicion.y, ancho, alto);
                batch.setColor(Color.WHITE);
            }
        } else {
            batch.draw(frameParaDibujar, posicion.x, posicion.y, ancho, alto);
        }
    }

    /**
     * Actualiza el estado recibido del servidor
     */
    public void actualizarDesdeServidor(float x, float y, String accion, String direccion) {
        this.posicion.set(x, y);

        try {
            Accion nuevaAccion = Accion.valueOf(accion);
            if (this.accionActual != nuevaAccion) {
                this.accionActual = nuevaAccion;
                this.estadoTiempo = 0; // Reiniciar animación
            }

            this.direccionActual = Direccion.valueOf(direccion);
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Estado inválido recibido: " + accion + ", " + direccion);
        }
    }

    public void mostrarEfectoEvasion(boolean mostrar) {
        this.mostrandoEvasion = mostrar;
        if (!mostrar) {
            this.mostrarFrame = true;
            this.parpadeoEvasion = 0f;
        }
    }

    public void dispose() {
        if (hojaSprite != null) {
            hojaSprite.dispose();
        }
    }

    // ==================== GETTERS ====================
    public int getNumeroJugador() { return numeroJugador; }
    public float getX() { return posicion.x; }
    public float getY() { return posicion.y; }
    public Vector2 getPosicion() { return posicion; }
    public Accion getAccionActual() { return accionActual; }
    public Direccion getDireccionActual() { return direccionActual; }

    // ==================== SETTERS ====================
    public void setX(float x) { this.posicion.x = x; }
    public void setY(float y) { this.posicion.y = y; }
    public void setAccionActual(Accion accion) {
        if (this.accionActual != accion) {
            this.accionActual = accion;
            this.estadoTiempo = 0;
        }
    }
    public void setDireccionActual(Direccion direccion) {
        this.direccionActual = direccion;
    }

    public Rectangle getHitbox() {
        hitbox.setPosition(posicion.x, posicion.y);
        return hitbox;
    }


    public int getMonedas() {
        return this.monedas;
    }

    public void setMonedas(int monedas) {
        this.monedas = monedas;
    }
}
