import java.io.*;
import java.net.*;
import java.util.*;

public class MyTCPSocket {
    private DatagramSocket socket;
    private InetAddress serverAddr;
    private int serverPort;
    private PerformanceMetrics metrics;
 
    private int cwnd = 1;          
    private int ssthresh = 8;       
    private int dupACKcount = 0;   
    private String mode;
    private int nextSeqNum = 1;       
    private int expectedAckNum = 1;
      
    private double srtt = 50.0;         
    private double rto = 150.0;         
    private final double alpha = 0.25;   
    private final double beta = 1.5;     
    private final int minRTO = 100;      
    private final int maxRTO = 500;               

    public MyTCPSocket(String mode, PerformanceMetrics metrics) throws Exception {
        this.mode = mode.toUpperCase();
        this.metrics = metrics;
        socket = new DatagramSocket();
        serverAddr = InetAddress.getByName("127.0.0.1");
        serverPort = 5000;

        handshake();
    }

 
    private void handshake() throws Exception {
        System.out.println("Starting TCP " + mode + " Mode");

 
        MyTCPPacket syn = new MyTCPPacket(0, 0, true, false, false, "");
        send(syn);

 
        MyTCPPacket synAck = receive();

        MyTCPPacket ack = new MyTCPPacket(1, synAck.seqNum + 1, false, true, false, "");
        send(ack);

        System.out.println("Handshake complete. Starting transmission...\n");
    }

 
    private void send(MyTCPPacket packet) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(packet);
        byte[] data = baos.toByteArray();

        DatagramPacket dp = new DatagramPacket(data, data.length, serverAddr, serverPort);
        socket.send(dp);
    }

    private MyTCPPacket receive() throws Exception {
        byte[] buf = new byte[4096];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        socket.receive(dp);

        ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (MyTCPPacket) ois.readObject();
    }

    private List<String> readDataFile(String filename) throws Exception {
        List<String> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {  
                    data.add(line.trim());
                }
            }
        }
        return data;
    }

 
    public void startTransmission(int totalRounds) throws Exception {
 
        List<String> fileData = readDataFile("sample_data.txt");
        int totalDataChunks = fileData.size();
        int currentDataIndex = 0;
        int globalPacketCounter = 1;
        
        System.out.println("Loaded " + totalDataChunks + " lines of data from sample_data.txt");
        System.out.println("Starting data transmission...\n");

        for (int round = 1; round <= totalRounds && currentDataIndex < totalDataChunks; round++) {
            long roundStartTime = System.currentTimeMillis();
            
            System.out.println("== TCP " + mode + " Mode ==");
            System.out.println("Round " + round + ": cwnd = " + cwnd + ", ssthresh = " + ssthresh);
            
            List<String> dataPackets = new ArrayList<>();
            StringBuilder sentPacketsStr = new StringBuilder("Sent packets: ");
            
            for (int i = 0; i < cwnd && currentDataIndex < totalDataChunks; i++) {
                String packetId = "pkt" + globalPacketCounter++;
                String actualData = fileData.get(currentDataIndex++);
                String packetData = packetId + ":" + actualData;
                dataPackets.add(packetData);
                
                if (i > 0) sentPacketsStr.append(", ");
                sentPacketsStr.append(packetId + " (\"" + 
                    (actualData.length() > 30 ? actualData.substring(0, 30) + "..." : actualData) + "\")");
            }

            System.out.println(sentPacketsStr.toString());
            String allPackets = String.join("|||", dataPackets);
            MyTCPPacket dataPacket = new MyTCPPacket(nextSeqNum, 0, false, false, false, allPackets);
            send(dataPacket);
            
            nextSeqNum += dataPackets.size();

            boolean lossEvent = false;
            int maxAcksToWait = Math.min(cwnd, 4); 
            String lastAckReceived = "";
            long startTime = System.currentTimeMillis(); 
            
            for (int ackAttempt = 0; ackAttempt < maxAcksToWait && !lossEvent; ackAttempt++) {
                try {
                    socket.setSoTimeout((int)rto);  
                    MyTCPPacket ack = receive();
                    
                   
                    long endTime = System.currentTimeMillis();
                    double rttSample = endTime - startTime;
                    updateRTO(rttSample);
                    
                    String currentAck = ack.data;
                    System.out.println("Received: " + currentAck + " (RTT: " + (int)rttSample + "ms, RTO: " + (int)rto + "ms)");
                    
                  
                    startTime = System.currentTimeMillis();
                    
                    if (ack.ackNum > expectedAckNum) {
                        expectedAckNum = ack.ackNum;
                    }
                    
                   
                    if (lastAckReceived.equals(currentAck) && !lastAckReceived.isEmpty()) {
                        dupACKcount++;
                        
                        if (dupACKcount >= 3) {
                            System.out.println("==> 3 Duplicate ACKs: Fast Retransmit triggered.");
                            ssthresh = Math.max(cwnd / 2, 1);

                            if (mode.equals("TAHOE")) {
                                cwnd = 1;
                                System.out.println("TCP TAHOE Reset: cwnd -> " + cwnd);
                            } else if (mode.equals("RENO")) {
                                cwnd = ssthresh;
                                System.out.println("TCP RENO Fast Recovery: cwnd -> " + cwnd);
                            }

                            dupACKcount = 0;
                            lossEvent = true;
                            break;
                        }
                    } else {
                        dupACKcount = 1;
                        lastAckReceived = currentAck;
                    }
                    
                } catch (SocketTimeoutException e) {
                   
                    System.out.println("Timeout occurred - assuming packet loss");
                    ssthresh = Math.max(cwnd / 2, 1);
                    cwnd = 1;
                    System.out.println("Timeout Reset: cwnd -> 1, ssthresh -> " + ssthresh);
                    lossEvent = true;
                    break;
                }
            }

            if (!lossEvent) {
                if (cwnd < ssthresh) {
                    cwnd *= 2;
                    System.out.println("Slow Start: cwnd -> " + cwnd);
                } else {
                    cwnd++;
                    System.out.println("Congestion Avoidance: cwnd -> " + cwnd);
                }
            }
 
            long roundEndTime = System.currentTimeMillis();
            long roundRTT = roundEndTime - roundStartTime;
            metrics.recordRound(round, cwnd, ssthresh, roundRTT, lossEvent, cwnd);

            System.out.println();
            Thread.sleep(1000);
        }
 
        MyTCPPacket fin = new MyTCPPacket(0, 0, false, false, true, "");
        send(fin);
        socket.close();
        
   
        System.out.println("\n=== Data Transmission Complete ===");
        System.out.println("Total data chunks transmitted: " + currentDataIndex + " / " + totalDataChunks);
        if (currentDataIndex >= totalDataChunks) {
            System.out.println("All data transmitted successfully!");
        } else {
            System.out.println("Transmission stopped early (" + (totalDataChunks - currentDataIndex) + " chunks remaining)");
        }
        System.out.println("Check 'received_data.txt' on server side for received data.");
        System.out.println("Client disconnected.");
    }

    
    private void updateRTO(double rttSample) {
      
        srtt = (1.0 - alpha) * srtt + alpha * rttSample;
        rto = beta * srtt;
        rto = Math.max(minRTO, Math.min(maxRTO, rto));
    }
}