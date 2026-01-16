import java.io.*;
import java.util.*;

public class TCPAnalyzer {
    public static void main(String[] args) {
        System.out.println("=== TCP Tahoe vs TCP Reno Performance Analysis ===\n");
        

        File tahoeFile = new File("TAHOE_metrics.csv");
        File renoFile = new File("RENO_metrics.csv");
        
        if (!tahoeFile.exists() || !renoFile.exists()) {
            System.out.println("Please run both TCP Tahoe and TCP Reno simulations first.");
            System.out.println("Missing files:");
            if (!tahoeFile.exists()) System.out.println("- TAHOE_metrics.csv");
            if (!renoFile.exists()) System.out.println("- RENO_metrics.csv");
            return;
        }
        
        try {
            Map<String, List<Double>> tahoeData = loadCSVData("TAHOE_metrics.csv");
            Map<String, List<Double>> renoData = loadCSVData("RENO_metrics.csv");
            
            generateComparison(tahoeData, renoData);         
        } catch (IOException e) {
            System.err.println("Error reading CSV files: " + e.getMessage());
        }
    }
    
    private static Map<String, List<Double>> loadCSVData(String filename) throws IOException {
        Map<String, List<Double>> data = new HashMap<>();
        data.put("Round", new ArrayList<>());
        data.put("CWND", new ArrayList<>());
        data.put("SSThresh", new ArrayList<>());
        data.put("RTT", new ArrayList<>());
        data.put("PacketLoss", new ArrayList<>());
        
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();  
        
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                data.get("Round").add(Double.parseDouble(parts[0]));
                data.get("CWND").add(Double.parseDouble(parts[1]));
                data.get("SSThresh").add(Double.parseDouble(parts[2]));
                data.get("RTT").add(Double.parseDouble(parts[3]));
                data.get("PacketLoss").add(Double.parseDouble(parts[4]));
            }
        }
        reader.close();
        return data;
    }
    
    private static void generateComparison(Map<String, List<Double>> tahoeData, Map<String, List<Double>> renoData) {
        System.out.println("=== Quantitative Comparison ===");
        
        // Calculate basic metrics
        double tahoeAvgCwnd = tahoeData.get("CWND").stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double renoAvgCwnd = renoData.get("CWND").stream().mapToDouble(Double::doubleValue).average().orElse(0);
        
        double tahoeMaxCwnd = tahoeData.get("CWND").stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double renoMaxCwnd = renoData.get("CWND").stream().mapToDouble(Double::doubleValue).max().orElse(0);
        
        double tahoeAvgRTT = tahoeData.get("RTT").stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double renoAvgRTT = renoData.get("RTT").stream().mapToDouble(Double::doubleValue).average().orElse(0);
        
        int tahoeLossEvents = (int) tahoeData.get("PacketLoss").stream().mapToDouble(Double::doubleValue).sum();
        int renoLossEvents = (int) renoData.get("PacketLoss").stream().mapToDouble(Double::doubleValue).sum();
        
        int tahoeRounds = tahoeData.get("Round").size();
        int renoRounds = renoData.get("Round").size();
        
     
        double tahoeTotalPackets = tahoeData.get("CWND").stream().mapToDouble(Double::doubleValue).sum();
        double renoTotalPackets = renoData.get("CWND").stream().mapToDouble(Double::doubleValue).sum();
        
        double tahoeThroughput = tahoeRounds > 0 ? tahoeTotalPackets / tahoeRounds : 0;
        double renoThroughput = renoRounds > 0 ? renoTotalPackets / renoRounds : 0;
   
        double tahoePacketLossRate = tahoeRounds > 0 ? (tahoeLossEvents * 100.0) / tahoeRounds : 0;
        double renoPacketLossRate = renoRounds > 0 ? (renoLossEvents * 100.0) / renoRounds : 0;
        
        System.out.printf("%-25s %-12s %-12s%n", "Metric", "TCP Tahoe", "TCP Reno");
        System.out.println("=======================================================");
        System.out.printf("%-25s %-12.2f %-12.2f%n", "Throughput (pkt/sec)", tahoeThroughput, renoThroughput);
        System.out.printf("%-25s %-12.2f %-12.2f%n", "Packet Loss Rate (%)", tahoePacketLossRate, renoPacketLossRate);
        System.out.printf("%-25s %-12.2f %-12.2f%n", "Round-Trip Time (ms)", tahoeAvgRTT, renoAvgRTT);
        System.out.println();
        System.out.printf("%-25s %-12.2f %-12.2f%n", "Avg CWND", tahoeAvgCwnd, renoAvgCwnd);
        System.out.printf("%-25s %-12.0f %-12.0f%n", "Max CWND", tahoeMaxCwnd, renoMaxCwnd);
        System.out.printf("%-25s %-12d %-12d%n", "Total Loss Events", tahoeLossEvents, renoLossEvents);
        System.out.printf("%-25s %-12d %-12d%n", "Total Rounds", tahoeRounds, renoRounds);
    }
}