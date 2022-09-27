package io.github.pablof036.tellosdk.implementation;

import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * Manages connection to Tello Drone
 */
public class Connection {
    private boolean connected;
    private CommandDispatcher commandDispatcher = new CommandDispatcher();
    private final BiConsumer<State, Throwable> onStateReceive;
    private StateServer stateServer;

    public Connection(BiConsumer<State, Throwable> onStateReceive) {
        this.onStateReceive = onStateReceive;
        if (onStateReceive != null) {
            stateServer = new StateServer(onStateReceive);
        }
    }

    public void connect() {
        commandDispatcher.start();
        if (stateServer != null) {
            stateServer.start();
        }
        connected = true;
    }

    public void disconnect() {
        commandDispatcher.interrupt();
        stateServer.interrupt();

        try {
            commandDispatcher.join();
            stateServer.join();
            commandDispatcher = new CommandDispatcher();
            stateServer = new StateServer(onStateReceive);
            connected = false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public CompletableFuture<Void> scheduleCommand(String message) {
        return CompletableFuture.supplyAsync(() -> {
            scheduleCommandAndWait(message);
            return null;
        });
    }

    public CompletableFuture<String> scheduleReadCommand(String message) {
        return CompletableFuture.supplyAsync(() -> scheduleCommandAndWait(message));
    }

    private String scheduleCommandAndWait(String message) {
        if (!connected) {
            throw new RuntimeException("Drone not connected");
        }

        Command command = new Command(message);
        commandDispatcher.queue.add(command);
        try {
            synchronized (command) {
                command.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (command.getThrowable() != null) {
            throw new RuntimeException(command.getThrowable());
        }

        return command.getResponse();
    }
}