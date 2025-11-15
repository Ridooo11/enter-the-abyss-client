package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.FontManager;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PantallaTienda extends Pantalla {

    private BitmapFont fontTitulo;
    private BitmapFont fontOpciones;
    private BitmapFont fontInfo;
    private Texture fondoTienda;
    private Texture heart100;
    private Texture moneda;

    private final String[] opciones = {"COMPRAR CORAZÓN", "VOLVER AL JUEGO"};
    private int opcionSeleccionada = 0;

    private GlyphLayout layout;
    private Viewport viewport;
    private OrthographicCamera camara;

    private Jugador jugador;
    private int precioCorazon = 20;

    private PantallaJuego pantallaJuego;

    // Animación
    private float tiempoParpadeo = 0;
    private float alphaParpadeo = 1f;

    // Mensajes de compra
    private String mensajeCompra = null;
    private float tiempoMensaje = 0;

    public PantallaTienda(Game juego, SpriteBatch batch, Jugador jugador, PantallaJuego pantallaJuego) {
        super(juego, batch);
        this.jugador = jugador;
        this.pantallaJuego = pantallaJuego;
    }

    @Override
    public void show() {
        fontTitulo = FontManager.getInstance().getTitulo();
        fontOpciones = FontManager.getInstance().getGrande();
        fontInfo = FontManager.getInstance().getMediana();
        layout = new GlyphLayout();

        camara = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camara);
        viewport.apply();
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();

        fondoTienda = new Texture("Fondos/OgroTienda.png");
        heart100 = new Texture("imagenes/corazon100%.png");
        moneda = new Texture("imagenes/moneda.png");

        System.out.println("✅ Tienda cargada correctamente (SERVIDOR)");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiempoParpadeo += delta;
        alphaParpadeo = 0.7f + 0.3f * (float) Math.sin(tiempoParpadeo * 4);

        manejarInput();

        camara.update();
        batch.setProjectionMatrix(camara.combined);

        float ancho = viewport.getWorldWidth();
        float alto = viewport.getWorldHeight();
        float centerX = ancho / 2f;

        batch.begin();

        // ===== FONDO =====
        batch.draw(fondoTienda, 0, 0, ancho, alto);

        // ===== TÍTULO =====
        fontTitulo.setColor(Color.WHITE);
        String titulo = "TIENDA DE OGRINI";
        layout.setText(fontTitulo, titulo);
        fontTitulo.draw(batch, titulo, centerX - layout.width / 2f, alto - 40);

        // ===== MONEDAS ARRIBA DERECHA =====
        float monedasX = ancho - 150;
        float monedasY = alto - 60;

        batch.draw(moneda, monedasX, monedasY - 95, 40, 40);

        fontInfo.setColor(Color.GOLD);
        String textoMonedas = "x " + jugador.getMonedas();
        fontInfo.draw(batch, textoMonedas, monedasX + 50, monedasY - 60);

        // ===== OPCIONES MÁS CHICAS Y MÁS ARRIBA =====
        fontOpciones.getData().setScale(0.75f);

        float startY = 310;     // debajo de la boca
        float espacioOpciones = 55f;

        for (int i = 0; i < opciones.length; i++) {
            String texto = opciones[i];
            layout.setText(fontOpciones, texto);

            float x = centerX - layout.width / 2f;
            float y = startY - (i * espacioOpciones);

            if (i == opcionSeleccionada) {
                fontOpciones.setColor(Color.GOLD);
                float offset = 5f * (float) Math.sin(tiempoParpadeo * 6);

                fontOpciones.draw(batch, ">", x - 40 - offset, y);
                fontOpciones.draw(batch, "<", x + layout.width + 20 + offset, y);
            } else {
                fontOpciones.setColor(0.7f, 0.7f, 0.7f, 0.8f);
            }

            fontOpciones.draw(batch, texto, x, y);
        }

        // ===== MENSAJE DE COMPRA =====
        if (mensajeCompra != null && tiempoMensaje > 0) {
            fontInfo.setColor(Color.WHITE);
            layout.setText(fontInfo, mensajeCompra);
            fontInfo.draw(batch, mensajeCompra,
                centerX - layout.width / 2f,
                70); // debajo del cartel SHOP
            tiempoMensaje -= delta;
        }

        batch.end();
    }

    private void manejarInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
            tiempoParpadeo = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
            tiempoParpadeo = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            switch (opcionSeleccionada) {
                case 0:
                    comprarCorazon();
                    break;
                case 1:
                    juego.setScreen(pantallaJuego);
                    break;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            juego.setScreen(pantallaJuego);
            Sonidos.reanudarMusicaJuego();
        }
    }

    private void comprarCorazon() {

        if (jugador.getMonedas() < precioCorazon) {
            Sonidos.reproducirCompraFallida();
            mensajeCompra = "No tenés suficientes monedas";
            tiempoMensaje = 2;
            return;
        }

        pantallaJuego.getClientThread().sendMessage("ComprarVida:" + precioCorazon);
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();
    }

    @Override
    public void dispose() {
        if (fondoTienda != null) fondoTienda.dispose();
        if (heart100 != null) heart100.dispose();
        if (moneda != null) moneda.dispose();
        System.out.println("🧹 Tienda limpiada (SERVIDOR)");
    }
}
