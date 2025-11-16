package com.abyssdev.entertheabyss.network;

import com.abyssdev.entertheabyss.interfaces.GameController;
import com.abyssdev.entertheabyss.mapas.Sala;
import com.abyssdev.entertheabyss.mapas.ZonaTransicion;
import com.abyssdev.entertheabyss.pantallas.PantallaJuego;
import com.abyssdev.entertheabyss.personajes.Enemigo;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/**
 * ClientThread - Versión CLIENTE
 * Maneja la comunicación del cliente con el servidor
 */
public class ClientThread extends Thread {

    private DatagramSocket socket;
    private int serverPort = 9999;
    private String ipServerStr = "255.255.255.255";
    private InetAddress ipServer;
    private boolean end = false;
    private PantallaJuego gameController;

    public ClientThread(PantallaJuego gameController) {
        try {
            this.gameController = gameController;
            ipServer = InetAddress.getByName(ipServerStr);
            socket = new DatagramSocket();
            socket.setSoTimeout(0); // Sin timeout

            System.out.println("🌐 Cliente creado. Servidor: " + ipServerStr + ":" + serverPort);
        } catch (SocketException | UnknownHostException e) {
            System.err.println("❌ Error al crear cliente: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("🔄 Cliente escuchando mensajes del servidor...");

        while (!end) {
            DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
            try {
                socket.receive(packet);
                processMessage(packet);
            } catch (IOException e) {
                if (!end) {
                    System.err.println("❌ Error al recibir paquete: " + e.getMessage());
                }
            }
        }

        System.out.println("🔴 Cliente desconectado");
    }

    private void processMessage(DatagramPacket packet) {
        String message = (new String(packet.getData())).trim();
        String[] parts = message.split(":");

        System.out.println("📨 Servidor: " + message);

        switch (parts[0]) {
            case "AlreadyConnected":
                System.out.println("⚠️ Ya estás conectado al servidor");
                break;

            case "Connected":
                // Connected:numPlayer
                if (parts.length >= 2) {
                    int numPlayer = Integer.parseInt(parts[1]);
                    System.out.println("✅ Conectado como jugador " + numPlayer);
                    this.ipServer = packet.getAddress();
                    gameController.connect(numPlayer);
                }
                break;

            case "Full":
                System.out.println("❌ Servidor lleno");
                this.end = true;
                break;

            case "Start":
                System.out.println("🎮 ¡Juego iniciado!");
                gameController.start();
                break;

            case "Update":
                // Update:tipo:id:x:y:action:direction
                if (parts.length >= 7) {
                    String tipo = parts[1];
                    int id = Integer.parseInt(parts[2]);
                    float x = Float.parseFloat(parts[3]);
                    float y = Float.parseFloat(parts[4]);
                    String action = parts[5];
                    String direction = parts[6];

                    if (tipo.equalsIgnoreCase("Jugador")) {
                        gameController.updatePlayerPosition(id, x, y);
                        gameController.updatePlayerAnimation(id, action, direction);
                    } else if (tipo.equalsIgnoreCase("Enemigo")) {
                        gameController.updateEnemyPosition(id, x, y);
                        gameController.updateEnemyAnimation(id, action, direction);
                    } else if (tipo.equalsIgnoreCase("Boss")) {
                        gameController.updateBossPosition(x, y);
                        gameController.updateBossAnimation(action, direction);
                    }
                }
                break;

            case "SpawnEnemy":
                // SpawnEnemy:id:x:y
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[1]);
                    float x = Float.parseFloat(parts[2]);
                    float y = Float.parseFloat(parts[3]);

                    Gdx.app.postRunnable(() -> {
                        gameController.spawnEnemy(id, x, y);
                    });
                }
                break;

            case "SpawnBoss":
                // SpawnBoss:x:y
                if (parts.length >= 3) {
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);

                    Gdx.app.postRunnable(() -> {
                        gameController.spawnBoss(x, y);
                    });
                }
                break;

            case "EnemyDead":
                // EnemyDead:enemyId
                if (parts.length >= 2) {
                    int enemyId = Integer.parseInt(parts[1]);
                    gameController.updateEnemyDead(enemyId);
                }
                break;

            case "BossDead":
                gameController.updateBossDead();
                break;

            case "UpdateCoins":
                // UpdateCoins:numPlayer:coins
                if (parts.length >= 3) {
                    int numPlayer = Integer.parseInt(parts[1]);
                    int coins = Integer.parseInt(parts[2]);
                    gameController.updateCoins(numPlayer, coins);
                }
                break;

            case "UpdateHealth":
                // UpdateHealth:numPlayer:health
                if (parts.length >= 3) {
                    int playerNum = Integer.parseInt(parts[1]);
                    int health = Integer.parseInt(parts[2]);
                    gameController.updateHealth(playerNum, health);
                }
                break;

            case "RoomChange":
                // RoomChange:roomId
                if (parts.length >= 2) {
                    String roomId = parts[1];
                    gameController.updateRoomChange(roomId);
                }
                break;

            // ✅ VERIFICAR EN processMessage() (línea ~93-102):
            case "Habilidades":
                // Habilidades:Vida Extra,0;Fuerza,1;...:150
                if (parts.length >= 2) {
                    // parts[1] contiene todo: "habilidades:monedas"
                    String datosCompletos = message.substring(message.indexOf(":") + 1);
                    Gdx.app.postRunnable(() -> {
                        gameController.mostrarArbolHabilidades(datosCompletos);
                    });
                }
                break;

            case "CompraExitosa":
                // CompraExitosa:nombreHabilidad:datosActualizados:monedas
                if (parts.length >= 4) {
                    String nombreHabilidad = parts[1];
                    String datosHabilidades = parts[2];
                    int monedas = Integer.parseInt(parts[3]);

                    Gdx.app.postRunnable(() -> {
                        gameController.actualizarHabilidades(datosHabilidades, monedas);
                        // Reproducir sonido de compra exitosa
                        Sonidos.reproducirCompraExitosa();
                    });
                }
                break;

            case "CompraFallida":
                // CompraFallida:nombreHabilidad
                if (parts.length >= 2) {
                    String nombreHabilidad = parts[1];
                    Gdx.app.postRunnable(() -> {
                        gameController.mostrarMensajeCompraFallida(nombreHabilidad);
                        Sonidos.reproducirCompraFallida();
                    });
                }
                break;

            case "CompraVidaExitosa":
                // CompraVidaExitosa:vidaNueva:monedasNuevas
                if (parts.length >= 3) {
                    int vidaNueva = Integer.parseInt(parts[1]);
                    int monedasNuevas = Integer.parseInt(parts[2]);

                    Gdx.app.postRunnable(() -> {
                        gameController.updateHealth(gameController.getMiNumeroJugador(), vidaNueva);
                        gameController.updateCoins(gameController.getMiNumeroJugador(), monedasNuevas);
                        Sonidos.reproducirCompraExitosa();
                        System.out.println("✅ Compra de vida exitosa! Vida: " + vidaNueva + ", Monedas: " + monedasNuevas);
                    });
                }
                break;

            case "CompraVidaFallida":
                // CompraVidaFallida:razon
                if (parts.length >= 2) {
                    String razon = parts[1];
                    Gdx.app.postRunnable(() -> {
                        Sonidos.reproducirCompraFallida();
                        System.out.println("❌ Compra de vida fallida: " + razon);
                    });
                }
                break;


            case "PlayerAttack":
                // PlayerAttack:numPlayer
                if (parts.length >= 2) {
                    int attackingPlayer = Integer.parseInt(parts[1]);
                    gameController.playerAttack(attackingPlayer);
                }
                break;


            case "WingmanDisconnected":
                // ✅ El otro jugador se desconectó
                if (parts.length >= 2) {
                    int numPlayerDesconectado = Integer.parseInt(parts[1]);
                    System.out.println("⚠️ Jugador " + numPlayerDesconectado + " se desconectó");
                }
                break;

            case "ForceDisconnect":

                System.out.println("🔴 Servidor forzó desconexión - Volviendo al menú");

                this.end = true; // ✅ Detener el hilo primero

                Gdx.app.postRunnable(() -> {
                    gameController.backToMenu();
                });
                break;

            case "Disconnect":
                System.out.println("🔌 Servidor desconectado");
                this.end = true; // ✅ Detener el hilo primero

                Gdx.app.postRunnable(() -> {
                    gameController.backToMenu();
                });
                break;

            case "NotConnected":
                System.out.println("⚠️ No estás conectado al servidor");
                break;

            case "DoorOpened":
                if (parts.length >= 2) {
                    String salaId = parts[1];
                    Gdx.app.postRunnable(() -> {
                        Sala sala = this.gameController.getMapaActual().getSala(salaId);
                        if (sala != null) {
                            sala.abrirPuertasDesdeServidor();
                        } else {
                            System.err.println("⚠️ Cliente: no se encontró sala " + salaId + " para DoorOpened");
                        }
                    });
                }
                break;


            case "SyncEnemies":
                // SyncEnemies:x1,y1;x2,y2;x3,y3...
                if (parts.length > 1) {
                    String enemiesData = parts[1];
                    gameController.syncEnemies(enemiesData);
                }
                break;

            case "PlayerDied":
                // PlayerDied:numPlayer
                if (parts.length >= 2) {
                    int playerNum = Integer.parseInt(parts[1]);
                    Gdx.app.postRunnable(() -> {
                        gameController.playerDied(playerNum);
                    });
                }
                break;

            case "GameOver":
                Gdx.app.postRunnable(() -> {
                    gameController.showGameOver();
                });
                break;

            case "WinGame":
                Gdx.app.postRunnable(() -> {
                    gameController.showWinGame();
                });
                break;



            default:
                System.out.println("⚠️ Mensaje desconocido: " + parts[0]);
                break;
        }
    }

    public void sendMessage(String message) {
        if (socket == null || socket.isClosed()) {
            System.err.println("⚠️ Socket cerrado, no se puede enviar: " + message);
            return;
        }

        byte[] byteMessage = message.getBytes();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, ipServer, serverPort);

        try {
            socket.send(packet);
            // System.out.println("📤 Enviado: " + message);
        } catch (IOException e) {
            System.err.println("❌ Error al enviar mensaje: " + e.getMessage());
        }
    }

    public void terminate() {
        System.out.println("🛑 Terminando cliente...");

        this.end = true;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        this.interrupt();
    }

    public void setServerIp(String ip) {
        this.ipServerStr = ip;
        try {
            this.ipServer = InetAddress.getByName(ip);
            System.out.println("🌐 IP del servidor actualizada a: " + ip);
        } catch (UnknownHostException e) {
            System.err.println("❌ IP inválida: " + e.getMessage());
        }
    }

    public void setServerPort(int port) {
        this.serverPort = port;
        System.out.println("🔌 Puerto del servidor actualizado a: " + port);
    }
}
