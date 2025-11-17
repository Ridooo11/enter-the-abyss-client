package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.Imagenes;

public class HabilidadEvasion extends Habilidad {
    public HabilidadEvasion() {
        super("Evasión", "Permite esquivar ataques rodando.", 35, Imagenes.getIconoBotasDoradas());
    }

    @Override
    public void aplicar(Jugador jugador) {
        //jugador.habilitarEvasion(true);
    }
}
