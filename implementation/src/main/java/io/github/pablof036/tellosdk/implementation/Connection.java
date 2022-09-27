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
    private DatagramSocket commandSocket;
    private StateServer stateServer;

    public void connect() {
        try {
            commandSocket = new DatagramSocket();
            commandSocket.setSoTimeout(5000);
            commandSocket.connect(InetAddress.getByName("192.168.10.1"), 8889);
        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e);
        }

        connected = true;
    }

    public void disconnect() {
        commandSocket.close();
        stopReceivingState();
        connected = false;
    }

    public void startReceivingState(BiConsumer<State, Throwable> stateCallback) {
        try {
            stateServer = new StateServer(stateCallback);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        stateServer.start();
    }

    public void stopReceivingState() {
        if (stateServer != null) {
            stateServer.interrupt();
            try {
                stateServer.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            stateServer = null;
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