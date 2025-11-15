package com.abyssdev.entertheabyss.interfaces;

public interface GameController {
    void connect(int numPlayer);
    void start();
    void updatePlayerPosition(int numPlayer, float x, float y);
    void updatePlayerAnimation(int numPlayer, String action, String direction);
    void updateEnemyPosition(int id, float x, float y);
    void updateEnemyAnimation(int id, String action, String direction);
    void updateEnemyDead(int enemyId);
    void updateBossDead();
    void updateCoins(int numPlayer, int coins);
    void updateHealth(int numPlayer, int health);
    void updateRoomChange(String roomId);
    void updateDoorOpened(String roomId);
    void playerAttack(int numPlayer);
    void backToMenu();
    void syncEnemies(String enemiesData);
    void spawnEnemy(int id, float x, float y);

    void mostrarArbolHabilidades(String datosHabilidades);
    void actualizarHabilidades(String datosHabilidades, int monedas);
    void mostrarMensajeCompraFallida(String nombreHabilidad);

    void spawnBoss(float x, float y);
    void updateBossPosition(float x, float y);
    void updateBossAnimation(String action, String direction);

    void playerDied(int numPlayer);
    void showGameOver();
    void mostrarMensajeDesconexion(String mensaje);
    int getMiNumeroJugador(); // ✅ NUEVO - Para saber qué jugador somos
}
