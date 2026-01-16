import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter TCP mode (TAHOE/RENO): ");
            System.out.println("Select Mode: ");
            System.out.println("1. TAHOE");
            System.out.println("2. RENO");
            
            int choice = sc.nextInt();
            String mode = "";
            if (choice == 1) {
                mode = "TAHOE";
            } else if (choice == 2) {
                mode = "RENO";
            } else {
                System.out.println("Invalid choice! Use either '1' for TAHOE or '2' for RENO.");
                sc.close();
                return;
            }
           
            if (!mode.equals("TAHOE") && !mode.equals("RENO")) {
                System.out.println("Invalid mode! Use either 'TAHOE' or 'RENO'.");
                sc.close();
                return;
            }

            PerformanceMetrics metrics = new PerformanceMetrics(mode);

            MyTCPSocket socket = new MyTCPSocket(mode, metrics);


            socket.startTransmission(15);

            metrics.generateReport();
            metrics.generateCSV();
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
