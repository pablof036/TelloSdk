package io.github.pablof036.tellosdk.implementation;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class CommandDispatcher extends Thread {
    protected final BlockingQueue<Command> queue;
    protected final DatagramSocket socket;

     protected CommandDispatcher() {
        this.queue = new LinkedBlockingQueue<>();
        
        try {
            this.socket = new DatagramSocket();
            this.socket.setSoTimeout(5000);
        } catch (SocketException e) {
            throw new RuntimeException("Could not create socket");
        }
    }


    @Override
    public void run() {
        try {
            socket.connect(InetAddress.getByName("192.168.10.1"), 8889);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        while (!Thread.interrupted()) {
            try {
                Command command = queue.take();
                byte[] bytes = command.getMessage().getBytes();

                try {
                    socket.send(new DatagramPacket(bytes, bytes.length));
                    DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(response);
                    String responseMessage = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
                    if (responseMessage.equals("error")) {
                        command.setThrowable(new RuntimeException("task failed"));
                    }
                    command.setResponse(responseMessage);
                } catch (IOException e) {
                    command.setThrowable(e);
                }

                synchronized (command) {
                    command.notify();
                }


            }  catch (InterruptedException e) {
                break;
            }
        }
        socket.disconnect();
    }
}
