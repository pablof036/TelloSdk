package io.github.pablof036.tellosdk.implementation;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * Manages connection to Tello Drone
 */
public class Connection {
    private boolean connected;
    private final DatagramSocket commandSocket;
    private final BiConsumer<State, Throwable> onStateReceive;
    private StateServer stateServer;

    public Connection(BiConsumer<State, Throwable> onStateReceive) {
        try {
            this.commandSocket = new DatagramSocket();
            this.commandSocket.setSoTimeout(2000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        this.onStateReceive = onStateReceive;
        if (onStateReceive != null) {
            stateServer = new StateServer(onStateReceive);
        }
    }

    public void connect() {
        try {
            commandSocket.connect(InetAddress.getByName("192.168.10.1"), 8889);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        if (stateServer != null) {
            stateServer.start();
        }
        connected = true;
    }

    public void disconnect() {
        commandSocket.disconnect();
        stateServer.interrupt();

        try {
            stateServer.join();
            stateServer = new StateServer(onStateReceive);
            connected = false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> scheduleCommand(String message) {
        return CompletableFuture.supplyAsync(() -> {
            sendCommand(message);
            return null;
        });
    }

    public CompletableFuture<String> scheduleReadCommand(String message) {
        return CompletableFuture.supplyAsync(() -> sendCommand(message));
    }

    private synchronized String sendCommand(String message) {
        if (!connected) {
            throw new RuntimeException("Drone not connected");
        }

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        String responseMessage;

        try {
            commandSocket.send(new DatagramPacket(bytes, bytes.length));
            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
            commandSocket.receive(response);
            responseMessage = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (responseMessage.equals("error")) {
            throw new RuntimeException("command failed");
        }

        return responseMessage;
    }
}