import java.io.*;
import java.net.*;
import java.util.*;

public class MyTCPServerSocket {
    private DatagramSocket socket;
    private int port;
    private Random random;

    private boolean connectionEstablished = false;
    private int expectedSeqNum = 1;
    private int nextSeqNum = 1;      

    public MyTCPServerSocket(int port) throws Exception {
        this.port = port;
        this.socket = new DatagramSocket(port);
        this.random = new Random();
        System.out.println("The server started on port " + port);
    }


    public void listen() throws Exception {
        InetAddress clientAddr = null;
        int clientPort = 0;
        
        while (true) {
            MyTCPPacket packet = receive();

           
            if (clientAddr == null) {
                clientAddr = packet.clientAddr;
                clientPort = packet.clientPort;
            }
 
            if (packet.FIN) {
                System.out.println("Received FIN. Closing connection...");
                
 
                MyTCPPacket finAck = new MyTCPPacket(0, packet.seqNum + 1, false, true, true, "");
                send(finAck, clientAddr, clientPort);
                
                socket.close();
                break;
            }

            if (packet.SYN && !connectionEstablished) {
                System.out.println("Client connected: /" + packet.clientAddr.getHostAddress());

                MyTCPPacket synAck = new MyTCPPacket(1, packet.seqNum + 1, true, true, false, "");
                send(synAck, packet.clientAddr, packet.clientPort);

                connectionEstablished = true;
                continue;
            }

            if (connectionEstablished && !packet.SYN && !packet.FIN) {
                handleDataTransmission(packet, clientAddr, clientPort);
            }
        }
    }

 
    private void handleDataTransmission(MyTCPPacket packet, InetAddress clientAddr, int clientPort) throws Exception {
        String receivedData = packet.data;  
        String[] dataPackets = receivedData.split("\\|\\|\\|");
        
        List<String> packetIds = new ArrayList<>();
        List<String> actualData = new ArrayList<>();
        
        System.out.println("Received " + dataPackets.length + " data packets:");
        for (String dataPacket : dataPackets) {
            String[] parts = dataPacket.split(":", 2);  
            if (parts.length == 2) {
                String packetId = parts[0];
                String data = parts[1];
                packetIds.add(packetId);
                actualData.add(data);
                
                String displayData = data.length() > 50 ? data.substring(0, 50) + "..." : data;
                System.out.println("  " + packetId + ": \"" + displayData + "\"");
                
                saveReceivedData(packetId, data);
            }
        }
        
    
      
        if (packetIds.isEmpty()) {
            System.out.println("No valid packets received");
            return;
        }
        
        if (random.nextInt(100) < 15) {
            System.out.println("Simulating complete ACK loss - no response sent");
            return;  
        }
        
        int lossIndex = random.nextInt(packetIds.size() + 1);
        
        if (lossIndex == packetIds.size()) {
            
            try {
                Thread.sleep(random.nextInt(50) + 10); 
            } catch (InterruptedException e) {}
            
            String lastPacketId = packetIds.get(packetIds.size() - 1);
            int ackNum = packet.seqNum + packetIds.size();   
            MyTCPPacket ack = new MyTCPPacket(nextSeqNum++, ackNum, false, true, false, "ACK:" + lastPacketId);
            expectedSeqNum = ackNum;   
            send(ack, clientAddr, clientPort);
        } else {
            String lostPacketId = packetIds.get(lossIndex);
            System.out.println("Simulating loss of: " + lostPacketId);
             
            if (lossIndex == 0) {
           
                String nextExpected = packetIds.get(0);
                int ackNum = expectedSeqNum;   
                MyTCPPacket ack = new MyTCPPacket(nextSeqNum++, ackNum, false, true, false, "ACK:" + nextExpected);
                send(ack, clientAddr, clientPort);
                
       
                for (int j = 0; j < 3; j++) {
                    MyTCPPacket dupAck = new MyTCPPacket(nextSeqNum++, ackNum, false, true, false, "ACK:" + nextExpected);
                    send(dupAck, clientAddr, clientPort);
                }
            } else {
                String lastInOrderPacket = packetIds.get(lossIndex - 1);
                int ackNum = packet.seqNum + lossIndex;   
                MyTCPPacket ack = new MyTCPPacket(nextSeqNum++, ackNum, false, true, false, "ACK:" + lastInOrderPacket);
                send(ack, clientAddr, clientPort);
                
                
                for (int j = 0; j < 3; j++) {
                    MyTCPPacket dupAck = new MyTCPPacket(nextSeqNum++, ackNum, false, true, false, "ACK:" + lastInOrderPacket);
                    send(dupAck, clientAddr, clientPort);
                }
            }
        }
    }
    
    private void saveReceivedData(String packetId, String data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("received_data.txt", true))) {
            writer.println("[" + packetId + "] " + data);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private MyTCPPacket receive() throws Exception {
        byte[] buf = new byte[4096];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        socket.receive(dp);

        ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MyTCPPacket packet = (MyTCPPacket) ois.readObject();

        packet.clientAddr = dp.getAddress();
        packet.clientPort = dp.getPort();
        return packet;
    }

    private void send(MyTCPPacket packet, InetAddress clientAddr, int clientPort) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(packet);
        byte[] data = baos.toByteArray();

        DatagramPacket dp = new DatagramPacket(data, data.length, clientAddr, clientPort);
        socket.send(dp);
    }
}
