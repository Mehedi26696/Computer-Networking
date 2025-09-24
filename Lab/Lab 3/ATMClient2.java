import java.io.*;
import java.net.*;
import java.util.UUID;

public class ATMClient2 {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private BufferedReader keyboard;
    private boolean loggedIn = false;
    private String currentCardNumber = "";
    private String transactionId = "";

    public void run() {
        try {
            socket = new Socket("10.33.26.156", 8080);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            keyboard = new BufferedReader(new InputStreamReader(System.in));
            
            String welcome = input.readUTF();
            System.out.println(welcome);
            
            while (true) {
                if (!loggedIn) {
                    login();
                } else {
                    showMenu();
                    String choice = keyboard.readLine();
                    
                    if (choice.equals("1")) {
                        checkBalance();
                    } else if (choice.equals("2")) {
                        withdraw();
                    } else if (choice.equals("3")) {
                        output.writeUTF("Exit");
                        break;
                    } else {
                        System.out.println("Invalid choice!");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }  
    }
    
    private void login() {
        try {
            System.out.print("Enter card number: ");
            String cardNumber = keyboard.readLine();
            System.out.print("Enter PIN: ");
            String pin = keyboard.readLine();

            output.writeUTF("LOGIN," + cardNumber + "," + pin);
            String response = input.readUTF();
            transactionId = UUID.randomUUID().toString().substring(0, 8);

            System.out.println("Server response: " + response);
            
            if (response.startsWith("LOGIN_SUCCESS")) {
                System.out.println("Login successful!");
                loggedIn = true;
                currentCardNumber = cardNumber;
            } else {
                System.out.println("Login failed! " + response);
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }
    
    private void showMenu() {
        System.out.println("\n--- ATM Menu ---");
        System.out.println("1. Check Balance");
        System.out.println("2. Withdraw Money");
        System.out.println("3. Exit");
        System.out.print("Choose option: ");
    }
    
    private void checkBalance() {
        try {
            output.writeUTF("BALANCE," + currentCardNumber);
            String response = input.readUTF();
            
            if (response.startsWith("BALANCE:")) {
                String balance = response.substring(8);
                System.out.println("Your balance: " + balance);
            } else {
                System.out.println("Error: " + response);
            }
        } catch (Exception e) {
            System.out.println("Balance error: " + e.getMessage());
        }
    }
    
    private void withdraw() {
        try {
            System.out.print("Enter amount to withdraw: ");
            String amountText = keyboard.readLine();
            double amount = Double.parseDouble(amountText);
            
            if (amount <= 0) {
                System.out.println("Amount must be positive!");
                return;
            }
            System.out.println("Transaction ID: " + transactionId);
            
            output.writeUTF("WITHDRAW," + currentCardNumber + "," + amount + "," + transactionId);
            String response = input.readUTF();
            
            System.out.println("Server response: " + response);
            
            if (response.startsWith("WITHDRAW_OK")) {
                System.out.println("Withdrawal successful!");
            } else if (response.startsWith("INSUFFICIENT_FUNDS")) {
                System.out.println("Not enough money in account!");
            } else {
                System.out.println("Withdrawal failed: " + response);
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format!");
        } catch (Exception e) {
            System.out.println("Withdrawal error: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        ATMClient2 client = new ATMClient2();
        client.run();
    }
}

