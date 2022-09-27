package io.github.pablof036.tellosdk.implementation;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;

class StateServer extends Thread {
    private final DatagramSocket socket;
    private final BiConsumer<io.github.pablof036.tellosdk.implementation.State, Throwable> onReceive;

    public StateServer(BiConsumer<io.github.pablof036.tellosdk.implementation.State, Throwable> onReceive) {
        Objects.requireNonNull(onReceive);
        this.onReceive = onReceive;

        try {
            socket = new DatagramSocket(8890);
            socket.setSoTimeout(250);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            DatagramPacket message = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(message);
                ForkJoinPool.commonPool().submit(() -> {
                    io.github.pablof036.tellosdk.implementation.State state = io.github.pablof036.tellosdk.implementation.State.parse(new String(message.getData(), 0, message.getLength(), StandardCharsets.UTF_8));
                    onReceive.accept(state, null);
                });
            } catch (IOException e) {
                onReceive.accept(null, e);
            }
        }
        socket.disconnect();
    }
}
