package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.Imagenes;

public class HabilidadDefensa extends Habilidad {
    public HabilidadDefensa() {
        super("Defensa", "Reduce el daño recibido.", 40, Imagenes.getIconoEscudo());
    }

    @Override
    public void aplicar(Jugador jugador) {
        //jugador.reducirDanioRecibido(0.2f); // 20% menos daño
    }
}
