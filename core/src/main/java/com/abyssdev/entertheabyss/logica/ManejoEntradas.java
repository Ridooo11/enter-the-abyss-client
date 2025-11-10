package com.abyssdev.entertheabyss.logica;

import com.abyssdev.entertheabyss.network.ClientThread;
import com.abyssdev.entertheabyss.personajes.Jugador;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class ManejoEntradas implements InputProcessor {
    private Jugador jugador;
    private ClientThread clientThread;

    private boolean arriba, abajo, izquierda, derecha;

    public ManejoEntradas(Jugador jugador, ClientThread clientThread) {
        this.jugador = jugador;
        this.clientThread = clientThread;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W: arriba = true; break;
            case Input.Keys.S: abajo = true; break;
            case Input.Keys.A: izquierda = true; break;
            case Input.Keys.D: derecha = true; break;
            case Input.Keys.SPACE:
                clientThread.sendMessage("Attack");
                break;
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                clientThread.sendMessage("Dash");
                break;
        }

        // Cada vez que cambia un input de movimiento, se manda el estado completo
        enviarEstado();
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W: arriba = false; break;
            case Input.Keys.S: abajo = false; break;
            case Input.Keys.A: izquierda = false; break;
            case Input.Keys.D: derecha = false; break;
        }

        enviarEstado();
        return true;
    }

    private void enviarEstado() {
        String mensaje = "Input:" + arriba + ":" + abajo + ":" + izquierda + ":" + derecha;
        clientThread.sendMessage(mensaje);
    }

    // Métodos no usados
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
