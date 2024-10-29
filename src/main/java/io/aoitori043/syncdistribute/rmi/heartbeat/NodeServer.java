package io.aoitori043.syncdistribute.rmi.heartbeat;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.database.DatabaseProperties;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NodeServer {
    private final String bcHost;
    private final int bcPort;
    private final int nodeId;
    private DatagramSocket socket;

    public NodeServer(String bcHost, int bcPort, int nodeId) throws Exception {
        this.bcHost = bcHost;
        this.bcPort = bcPort;
        this.nodeId = nodeId;
        this.socket = new DatagramSocket();
    }

    public void startHeartbeat() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void sendHeartbeat() throws Exception {
        String message = "H" + nodeId;
        byte[] buffer = message.getBytes();

        InetAddress address = InetAddress.getByName(bcHost);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, bcPort);
        socket.send(packet);
    }

    public static void start(){
        try {
            NodeServer node = new NodeServer(DatabaseProperties.bc$host, DatabaseProperties.bc$heartBeatPort, AoitoriProject.port);
            node.startHeartbeat();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
