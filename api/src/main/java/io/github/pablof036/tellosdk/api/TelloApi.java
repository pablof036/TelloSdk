package io.github.pablof036.tellosdk.api;

import io.github.pablof036.tellosdk.implementation.Connection;
import io.github.pablof036.tellosdk.implementation.State;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Interface with Tello Drone. Schedules command and receives state updates.
 * Note: the drone returns a response to any command immediately, even if the command is not done. If another command is sent while the
 * previous command is not done it will be ignored.
 */
public class TelloApi {

    private final List<BiConsumer<State, Throwable>> stateListeners = new ArrayList<>();
    private final Connection connection = new Connection((s, t) -> stateListeners.forEach(c -> c.accept(s, t)));

    /**
     * Opens connection with drone and enters SDK mode.
     * Will be completed with an exception if the connection failed.
     */
    public CompletableFuture<Void> connect() {
        return CompletableFuture.supplyAsync(() -> {
                    connection.connect();
                    return null;
                }).thenCompose(p -> connection.scheduleCommand("command"))
                .whenComplete((n, t) -> {
                    if (t != null) {
                        connection.disconnect();
                    }
                });
    }

    /**
     * Closes connection with drone
     */
    public void disconnect() {
        connection.disconnect();
    }

    /**
     * Adds a callback that will be used each time a state update is received.
     * The callback will be passed the most recent parsed state and, if any exception arose, a throwable parameter with that exception.
     *
     * @param listener callback
     */
    public void addStateListener(BiConsumer<State, Throwable> listener) {
        stateListeners.add(listener);
    }

    /**
     * Removes a state update listener.
     *
     * @param listener listener to be removed
     */
    public void removeStateListener(BiConsumer<State, Throwable> listener) {
        stateListeners.remove(listener);
    }

    /**
     * Read Command. Get current drone speed in cm/s.
     *
     * @return drone speed in cm/s (10-100).
     */
    public CompletableFuture<Integer> getSpeed() {
        return connection.scheduleReadCommand("speed?")
                .thenApply(Integer::parseInt);
    }

    /**
     * Read command. Get current battery level.
     *
     * @return battery level (0-100).
     */
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

    /**
     * Control command. Starts auto takeoff.
     */
    public CompletableFuture<Void> takeOff() {
        return connection.scheduleCommand("takeoff");
    }

    /**
     * Control command. Starts auto landing.
     */
    public CompletableFuture<Void> land() {
        return connection.scheduleCommand("land");
    }

    /**
     * Control command. Starts video stream on UDP port 11111.
     */
    public CompletableFuture<Void> startVideoStream() {
        return connection.scheduleCommand("streamon");
    }

    /**
     * Control command. Stops video stream.
     */
    public CompletableFuture<Void> stopVideoStream() {
        return connection.scheduleCommand("streamoff");
    }

    /**
     * Control command. Moves drone forward with the given distance.
     * Will throw is distance parameter is not within its bounds (20-500cm).
     *
     * @param distance distance in cm (20-500)
     */
    public CompletableFuture<Void> goForward(int distance) {
        return directionCommand("forward", distance);
    }

    /**
     * Control command. Moves drone backward with the given distance.
     * Will throw is distance parameter is not within its bounds (20-500cm).
     *
     * @param distance distance in cm (20-500)
     */
    public CompletableFuture<Void> goBackwards(int distance) {
        return directionCommand("back", distance);
    }

    /**
     * Control command. Moves drone up with the given distance.
     * Will throw is distance parameter is not within its bounds (20-500cm).
     *
     * @param distance distance in cm (20-500)
     */
    public CompletableFuture<Void> goUp(int distance) {
        return directionCommand("up", distance);
    }

    /**
     * Control command. Moves drone down with the given distance.
     * Will throw is distance parameter is not within its bounds (20-500cm).
     *
     * @param distance distance in cm (20-500)
     */
    public CompletableFuture<Void> goDown(int distance) {
        return directionCommand("down", distance);
    }

    /**
     * Control command. Moves drone left with the given distance.
     * Will throw is distance parameter is not within its bounds (20-500cm).
     *
     * @param distance distance in cm (20-500)
     */
    public CompletableFuture<Void> goLeft(int distance) {
        return directionCommand("left", distance);
    }

    /**
     * Control command. Moves drone right with the given distance.
     * Will throw is distance parameter is not within its bounds (20-500cm).
     *
     * @param distance distance in cm (20-500)
     */
    public CompletableFuture<Void> goRight(int distance) {
        return directionCommand("right", distance);
    }

    /**
     * Control command. Rotates drone clockwise with the given degrees.
     * Will throw if the rotation parameter is not within its bounds (0-360º).
     *
     * @param degrees degrees (0-360)
     */
    public CompletableFuture<Void> rotateClockwise(int degrees) {
        return rotationCommand("cw", degrees);
    }

    /**
     * Control command. Rotates drone counterclockwise with the given degrees.
     * Will throw if the rotation parameter is not within its bounds (0-360º).
     *
     * @param degrees degrees (0-360)
     */
    public CompletableFuture<Void> rotateCounterclockwise(int degrees) {
        return rotationCommand("ccw", degrees);
    }

    /**
     * Control command. Starts a flip forwards.
     */
    public CompletableFuture<Void> doForwardFlip() {
        return connection.scheduleCommand("flip f");
    }

    /**
     * Control command. Starts a flip backwards.
     */
    public CompletableFuture<Void> doBackwardsFlip() {
        return connection.scheduleCommand("flip b");
    }

    /**
     * Control command. Starts a left flip.
     */
    public CompletableFuture<Void> doLeftFlip() {
        return connection.scheduleCommand("flip l");
    }

    /**
     * Control command. Starts a right flip.
     */
    public CompletableFuture<Void> doRightFlip() {
        return connection.scheduleCommand("flip r");
    }

}
