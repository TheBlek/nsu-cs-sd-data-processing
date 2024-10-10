package ru.nsu.kuklin;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Main {
    public static void main(String[] args) {
        var listenPort = 8080;
        var remoteIp = "127.0.0.1";
        var remotePort = 8081;
        var remoteAddress = new InetSocketAddress(remoteIp, remotePort);
        AsynchronousServerSocketChannel serverSocket;
        try {
            serverSocket = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1", listenPort));
        } catch (Exception e) {
            System.out.println("Exception while creating serverSocket" + e);
            return;
        }
        if (!serverSocket.isOpen()) {
            System.out.println("Its closed mate");
        } else {
            System.out.println("Its open mate");
        }
        serverSocket.accept(null, new ClientConnectionHandler(remoteAddress, serverSocket));
        while (true) {}
    }

    private static class RemoteConnectionHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {
        private final AsynchronousSocketChannel serverConn;

        public RemoteConnectionHandler(AsynchronousSocketChannel serverConn) {
            this.serverConn = serverConn;
        }

        @Override
        public void completed(Void result, AsynchronousSocketChannel channel) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer, buffer, new ReadHandler(channel));
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel channel) {
            try {
                channel.close();
            } catch (Exception e) {
                System.out.println("Failed to close client channel " + e);
            }
            System.out.println("Failed to connect to remote. Closing client. " + exc);
        }

        private class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
            private final AsynchronousSocketChannel channel;

            public ReadHandler(AsynchronousSocketChannel channel) {
                this.channel = channel;
            }

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (attachment.position() == 0) {
                    try {
                        channel.close();
                        serverConn.close();
                    } catch (Exception e) {
                        System.out.println("Failed to close channel: " + e);
                    }
                    return;
                }
                attachment.position(0);
                serverConn.write(attachment, channel, new WriteHandler());
                ByteBuffer newBuffer = ByteBuffer.allocate(1024);
                channel.read(newBuffer, newBuffer, this);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("Read operation failed: " + exc);
            }

            private class WriteHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
                @Override
                public void completed(Integer result, AsynchronousSocketChannel channel) {
                    System.out.println("Successfully answered");
                }

                @Override
                public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                    try {
                        channel.close();
                    } catch (Exception e) {
                        System.out.println("Failed to close client channel " + e);
                    }
                    System.out.println("Failed to write to the remote");
                }
            }
        }
    }

    private static class ClientConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        private final InetSocketAddress remoteAddress;
        private final AsynchronousServerSocketChannel serverSocket;

        public ClientConnectionHandler(InetSocketAddress remoteAddress, AsynchronousServerSocketChannel serverSocket) {
            this.remoteAddress = remoteAddress;
            this.serverSocket = serverSocket;
        }

        @Override
        public void completed(AsynchronousSocketChannel channel, Object attachment) {
            try {
                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            } catch (Exception e) {
                System.out.println("Failed to set tcp nodelay");
            }
            AsynchronousSocketChannel serverConn;
            try {
                serverConn = AsynchronousSocketChannel.open();
            } catch (Exception e) {
                System.out.println("Failed to open socket channel: " + e);
                return;
            }
            serverConn.connect(remoteAddress, channel, new RemoteConnectionHandler(serverConn));
            serverSocket.accept(attachment, this);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("Failed to accept connection: " + exc);
        }
    }
}