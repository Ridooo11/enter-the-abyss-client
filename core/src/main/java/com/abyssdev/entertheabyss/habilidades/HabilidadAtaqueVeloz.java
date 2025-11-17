package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.Imagenes;

public class HabilidadAtaqueVeloz extends Habilidad {
    public HabilidadAtaqueVeloz() {
        super("Ataque Veloz", "Aumenta la velocidad de ataque.", 25, Imagenes.getIconoEspadaDoble());
    }

    @Override
    public void aplicar(Jugador jugador) {
       // jugador.reducirCooldownAtaque(0.3f);
    }
}
