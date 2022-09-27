package io.github.pablof036.tellosdk.api;

import io.github.pablof036.tellosdk.implementation.Connection;
import io.github.pablof036.tellosdk.implementation.State;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Interface with Tello Drone. Schedules command and receiver state updates.
 */
public class TelloApi {

    private final List<BiConsumer<State, Throwable>> stateListeners = new ArrayList<>();
    private final Connection connection = new Connection((s, t) -> stateListeners.forEach(c -> c.accept(s, t)));

    public CompletableFuture<Void> connect() {
        connection.connect();
        return connection.scheduleCommand("command")
                .whenComplete((m, t) -> {
                    if (t != null)  {
                        disconnect();
                    }
                });
    }

    public void disconnect() {
        connection.disconnect();
    }

    public void addStateListener(BiConsumer<State, Throwable> listener) {
        stateListeners.add(listener);
    }

    public void removeStateListener(BiConsumer<State, Throwable> listener) {
        stateListeners.remove(listener);
    }

    public CompletableFuture<Integer> getSpeed() {
        return connection.scheduleReadCommand("speed?")
                .thenApply(Integer::parseInt);
    }

    public CompletableFuture<Integer> getBatteryLevel() {
        return connection.scheduleReadCommand("bat?")
                .thenApply(Integer::parseInt);
    }

    private CompletableFuture<Void> directionCommand(String command, int distance) {
        if (distance < 20 || distance > 500) {
            throw new IllegalArgumentException("distance must be between 20 and 500cm");
        }
        return connection.scheduleCommand(String.format("%s %s", command, distance));
    }
    private CompletableFuture<Void> rotationCommand(String command, int degrees) {
        if (degrees < 0 || degrees > 360) {
            throw new IllegalArgumentException("rotation must be between 0 and 360 degrees");
        }
        return connection.scheduleCommand(String.format("%s %s", command, degrees));
    }

    public CompletableFuture<Void> takeOff() {
        return connection.scheduleCommand("takeoff");
    }

    public CompletableFuture<Void> land() {
        return connection.scheduleCommand("land");
    }

    public CompletableFuture<Void> startVideoStream() { return connection.scheduleCommand("streamon"); }

    public CompletableFuture<Void> stopVideoStream() { return connection.scheduleCommand("streamoff"); }

    public CompletableFuture<Void> goForward(int distance) {
        return directionCommand("forward", distance);
    }

    public CompletableFuture<Void> goBackwards(int distance) {
        return directionCommand("back", distance);
    }

    public CompletableFuture<Void> goUp(int distance) {
        return directionCommand("up", distance);
    }

    public CompletableFuture<Void> goDown(int distance) {
        return directionCommand("down", distance);
    }

    public CompletableFuture<Void> goLeft(int distance) {
        return directionCommand("left", distance);
    }

    public CompletableFuture<Void> goRight(int distance) {
        return directionCommand("right", distance);
    }

    public CompletableFuture<Void> rotateClockwise(int degrees) {
        return rotationCommand("cw", degrees);
    }

    public CompletableFuture<Void> rotateCounterclockwise(int degrees) {
        return rotationCommand("ccw", degrees);
    }

    public CompletableFuture<Void> doForwardFlip() {
        return connection.scheduleCommand("flip f");
    }

    public CompletableFuture<Void> doBackwardsFlip() {
        return connection.scheduleCommand("flip b");
    }

    public CompletableFuture<Void> doLeftFlip() {
        return connection.scheduleCommand("flip l");
    }

    public CompletableFuture<Void> doRightFlip() {
        return connection.scheduleCommand("flip r");
    }

}
