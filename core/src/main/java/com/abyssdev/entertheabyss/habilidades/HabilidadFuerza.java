package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.Imagenes;

public class HabilidadFuerza extends Habilidad {
    public HabilidadFuerza() {
        super("Fuerza", "Aumenta el daño de ataque.", 15, Imagenes.getIconoEspada());
    }

    @Override
    public void aplicar(Jugador jugador) {

        //jugador.aumentarDanio(1);
    }
}
