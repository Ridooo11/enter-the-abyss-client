package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.Imagenes;

public class HabilidadVelocidad2 extends Habilidad {
    public HabilidadVelocidad2() {
        super("Velocidad II",
            "Aumenta aún más la velocidad de movimiento.",
            25,
            Imagenes.getIconoBotas2());
    }

    @Override
    public void aplicar(Jugador jugador) {

        //jugador.aumentarVelocidad(1.2f);
    }
}
