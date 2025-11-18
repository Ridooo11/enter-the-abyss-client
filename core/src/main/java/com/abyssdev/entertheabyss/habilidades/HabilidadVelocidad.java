package com.abyssdev.entertheabyss.habilidades;
import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.Imagenes;

public class HabilidadVelocidad extends Habilidad {
    public HabilidadVelocidad() {
        super("Velocidad",
            "Aumenta la velocidad del jugador.",
            20 ,
            Imagenes.getIconoBotas());
    }

    @Override
    public void aplicar(Jugador jugador) {

        //jugador.aumentarVelocidad(0.8f);
    }
}
