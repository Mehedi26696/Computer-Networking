import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BankServer_13_47 { 
    private static final int PORT = 8080;
    private static String[] cardNumbers;
    private static String[] pins;
    private static String[] names;
    private static double[] balances;
    
    private static Map<String, String> processedTransactions = new ConcurrentHashMap<>();
    
    private static void loadClientData() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("clients_13_47.txt"));
            String line;
            int count = 0;
    
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    count++;
                }
            }
            reader.close();
             
            cardNumbers = new String[count];
            pins = new String[count];
            names = new String[count];
            balances = new double[count];

            reader = new BufferedReader(new FileReader("clients_13_47.txt"));
            int i = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    cardNumbers[i] = parts[0].trim();
                    pins[i] = parts[1].trim();
                    names[i] = parts[2].trim();
                    balances[i] = Double.parseDouble(parts[3].trim());
                    i++;
                }
            }
            reader.close();
            System.out.println("Loaded " + count + " clients from file");
            
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static synchronized void updateClientData() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("clients_13_47.txt"));
            for (int i = 0; i < cardNumbers.length; i++) {
                writer.write(cardNumbers[i] + "," + pins[i] + "," + names[i] + "," + balances[i]);
                writer.newLine();
            }
            writer.close();
            System.out.println("Client data updated in file.");
        } catch (Exception e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Bank Server Started on Port " + PORT);
        
        loadClientData();  
        try (ServerSocket server = new ServerSocket(PORT)) {
            
            while (true) {
                Socket client = server.accept();
                System.out.println("ATM Connected from: " + client.getInetAddress());
                
                Thread clientThread = new Thread(new ClientHandler(client));
                clientThread.start();
            }
        } catch (Exception e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }
    
    static class ClientHandler implements Runnable {
        private Socket client;
        
        public ClientHandler(Socket client) {
            this.client = client;
        }
        
        public void run() {
            handleClient(client);
        }
    }
    
    private static void handleClient(Socket client) {
        try {
            DataInputStream input = new DataInputStream(client.getInputStream());
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
 
            output.writeUTF("Welcome to The Bank!");
            
            String message;
            while (true) {
                message = input.readUTF();
                System.out.println("Received from " + client.getInetAddress() + ": " + message);
                
                if (message.equals("Exit")) {
                    output.writeUTF("End");
                    break;
                }
                
                String response = processMessage(message);
                output.writeUTF(response);
                System.out.println("Sent response: " + response);
            }
            
            client.close();
            System.out.println("ATM Disconnected: " + client.getInetAddress());
            
        } catch (Exception e) {
            System.out.println("Client Handler Error: " + e.getMessage());
        }
    }
    
    private static synchronized String processMessage(String message) {
        String[] parts = message.split(",");
        String command = parts[0];
        
        try {
            if (command.equals("LOGIN")) {
                if (parts.length >= 3) {
                    return handleLogin(parts[1], parts[2]);
                } else {
                    return "ERROR:Invalid login format";
                }
            }
            else if (command.equals("BALANCE")) {
                if (parts.length >= 2) {
                    return handleBalance(parts[1]);
                } else {
                    return "ERROR:Invalid balance request format";
                }
            }
            else if (command.equals("WITHDRAW")) {
                if (parts.length >= 4) {
                    String cardNo = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    String transactionId = parts[3];  
                    
                    return handleWithdrawWithTransactionId(cardNo, amount, transactionId);
                } else {
                    return "ERROR";
                }
            }
            else {
                return "ERROR:Unknown command - " + command;
            }
        } catch (NumberFormatException e) {
            return "ERROR:Invalid number format";
        } catch (Exception e) {
            return "ERROR:Processing error - " + e.getMessage();
        }
    }
    
    private static String handleLogin(String cardNo, String pin) {
        for (int i = 0; i < cardNumbers.length; i++) {
            if (cardNumbers[i].equals(cardNo) && pins[i].equals(pin)) {
                return "LOGIN_SUCCESS:" + names[i];
            }
        }
        return "LOGIN_FAIL:Invalid card number or PIN";
    }
    
    private static String handleBalance(String cardNo) {
        for (int i = 0; i < cardNumbers.length; i++) {
            if (cardNumbers[i].equals(cardNo)) { 
                return "BALANCE:" + balances[i];
            }
        }
        return "ERROR:Card not found";
    }
    
    private static String handleWithdrawWithTransactionId(String cardNo, double amount, String transactionId) {
        System.out.println("Processing withdrawal - Card: " + cardNo + ", Amount: " + amount + ", TxnID: " + transactionId);
        
        if (processedTransactions.containsKey(transactionId)) {
            String res = "Duplicate Transaction Detected";
            System.out.println("DUPLICATE TRANSACTION DETECTED: " + transactionId);
            return res;
        }
        
        for (int i = 0; i < cardNumbers.length; i++) {
            if (cardNumbers[i].equals(cardNo)) {
                String response;
                
                if (balances[i] >= amount) {
                    balances[i] = balances[i] - amount;
                    updateClientData(); 
                    response = "WITHDRAW_OK";
                    System.out.println("Withdrawal successful - Amount: $" + amount);
                } else {
                    response = "INSUFFICIENT_FUNDS";
                }
                
                processedTransactions.put(transactionId, response);
                return response;
            }
        }
        return "ERROR:Card not found";
    }
}
