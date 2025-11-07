package com.abyssdev.entertheabyss.network;

import com.abyssdev.entertheabyss.interfaces.GameController;

import java.io.IOException;
import java.net.*;

public class ClientThread extends Thread {

    private DatagramSocket socket;
    private int serverPort = 9999;
    private String ipServerStr = "127.0.0.1";
    private InetAddress ipServer;
    private boolean end = false;
    private GameController gameController;

    public ClientThread(GameController gameController) {
        try {
            this.gameController = gameController;
            ipServer = InetAddress.getByName(ipServerStr);
            socket = new DatagramSocket();
            System.out.println("🌐 Cliente creado. Conectando a " + ipServerStr + ":" + serverPort);
        } catch (SocketException | UnknownHostException e) {
            System.err.println("❌ Error al crear cliente: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("🔄 Cliente escuchando mensajes del servidor...");
        do {
            DatagramPacket packet = new DatagramPacket(new byte[2048], 2048); // Aumentado para enemigos
            try {
                socket.receive(packet);
                processMessage(packet);
            } catch (IOException e) {
                if(!end) {
                    System.err.println("❌ Error al recibir paquete: " + e.getMessage());
                }
            }
        } while(!end);
        System.out.println("🔴 Cliente desconectado");
    }

    private void processMessage(DatagramPacket packet) {
        String message = (new String(packet.getData())).trim();
        String[] parts = message.split(":");

        System.out.println("📨 Mensaje recibido: " + message);

        switch(parts[0]){
            case "AlreadyConnected":
                System.out.println("⚠️ Ya estás conectado al servidor");
                break;

            case "Connected":
                System.out.println("✅ Conectado al servidor como jugador " + parts[1]);
                this.ipServer = packet.getAddress();
                gameController.connect(Integer.parseInt(parts[1]));
                break;

            case "Full":
                System.out.println("❌ Servidor lleno");
                this.end = true;
                break;

            case "Start":
                System.out.println("🎮 ¡Juego iniciado!");
                this.gameController.start();
                break;

            case "UpdatePosition":
                // UpdatePosition:Player:numPlayer:x:y:action:direction
                if(parts[1].equals("Player")) {
                    int numPlayer = Integer.parseInt(parts[2]);
                    float x = Float.parseFloat(parts[3]);
                    float y = Float.parseFloat(parts[4]);
                    String action = parts.length > 5 ? parts[5] : "ESTATICO";
                    String direction = parts.length > 6 ? parts[6] : "ABAJO";

                    this.gameController.updatePlayerPosition(numPlayer, x, y);
                    this.gameController.updatePlayerAnimation(numPlayer, action, direction);
                }
                break;

            case "EnemyDead":
                // EnemyDead:enemyId
                int enemyId = Integer.parseInt(parts[1]);
                this.gameController.updateEnemyDead(enemyId);
                break;

            case "BossDead":
                this.gameController.updateBossDead();
                break;

            case "UpdateCoins":
                // UpdateCoins:numPlayer:coins
                int numPlayer = Integer.parseInt(parts[1]);
                int coins = Integer.parseInt(parts[2]);
                this.gameController.updateCoins(numPlayer, coins);
                break;

            case "UpdateHealth":
                // UpdateHealth:numPlayer:health
                int playerNum = Integer.parseInt(parts[1]);
                int health = Integer.parseInt(parts[2]);
                this.gameController.updateHealth(playerNum, health);
                break;

            case "RoomChange":
                // RoomChange:roomId
                String roomId = parts[1];
                this.gameController.updateRoomChange(roomId);
                break;

            case "PlayerAttack":
                // PlayerAttack:numPlayer
                int attackingPlayer = Integer.parseInt(parts[1]);
                this.gameController.playerAttack(attackingPlayer);
                break;

            case "EndGame":
                // EndGame:winner
                int winner = Integer.parseInt(parts[1]);
                this.gameController.endGame(winner);
                break;

            case "Disconnect":
                System.out.println("🔌 Servidor desconectado");
                this.gameController.backToMenu();
                break;

            case "NotConnected":
                System.out.println("⚠️ No estás conectado al servidor");
                break;

            case "SyncEnemies":
                // SyncEnemies:x1,y1;x2,y2;x3,y3...
                if(parts.length > 1) {
                    String enemiesData = parts[1];
                    this.gameController.syncEnemies(enemiesData);
                }
                break;
        }
    }

    public void sendMessage(String message) {
        byte[] byteMessage = message.getBytes();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, ipServer, serverPort);
        try {
            socket.send(packet);
            System.out.println("📤 Enviado: " + message);
        } catch (IOException e) {
            System.err.println("❌ Error al enviar mensaje: " + e.getMessage());
        }
    }

    public void terminate() {
        this.end = true;
        if(socket != null && !socket.isClosed()) {
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
}
