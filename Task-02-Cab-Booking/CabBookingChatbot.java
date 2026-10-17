import java.util.Scanner;

public class CabBookingChatbot {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Predefined routes
        String[] sources = {"Pune Station", "Swargate", "Hinjawadi"};
        String[] destinations = {"Shivajinagar", "Kothrud", "Wakad"};
        int[] distances = {5, 8, 10};

        // Cab rates per km
        String[] cabNames = {"Auto", "Sedan", "SUV"};
        int[] cabRates = {12, 18, 25};

        while (true) {
            System.out.println("\n===== CAB BOOKING CHATBOT =====");
            System.out.println("1. Book Ride");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            String mainInput = sc.nextLine().trim();
            int mainChoice;
            try {
                mainChoice = Integer.parseInt(mainInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                continue;
            }

            if (mainChoice == 2) {
                System.out.println("Thank you for using Cab Booking Chatbot. Goodbye!");
                break;
            }

            if (mainChoice != 1) {
                System.out.println("Invalid choice! Please try again.");
                continue;
            }

            // Choose cab type
            System.out.println("\n--- Choose Cab Type ---");
            System.out.println("1. Auto  (Rs.12/km)");
            System.out.println("2. Sedan (Rs.18/km)");
            System.out.println("3. SUV   (Rs.25/km)");
            System.out.print("Enter your choice: ");

            String cabInput = sc.nextLine().trim();
            int cabChoice;
            try {
                cabChoice = Integer.parseInt(cabInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Returning to main menu.");
                continue;
            }

            if (cabChoice < 1 || cabChoice > 3) {
                System.out.println("Invalid cab choice! Returning to main menu.");
                continue;
            }
            int cabIndex = cabChoice - 1;

            // Choose route
            System.out.println("\n--- Choose Route ---");
            for (int i = 0; i < 3; i++) {
                System.out.println((i + 1) + ". " + sources[i] + " -> " + destinations[i]
                        + " (" + distances[i] + " km)");
            }
            System.out.print("Enter your choice: ");

            String routeInput = sc.nextLine().trim();
            int routeChoice;
            try {
                routeChoice = Integer.parseInt(routeInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Returning to main menu.");
                continue;
            }

            if (routeChoice < 1 || routeChoice > 3) {
                System.out.println("Invalid route choice! Returning to main menu.");
                continue;
            }
            int routeIndex = routeChoice - 1;

            // Calculate fare
            int distance = distances[routeIndex];
            int rate = cabRates[cabIndex];
            int fare = rate * distance;

            System.out.println("\n--- Ride Summary ---");
            System.out.println("Cab Type    : " + cabNames[cabIndex]);
            System.out.println("Source      : " + sources[routeIndex]);
            System.out.println("Destination : " + destinations[routeIndex]);
            System.out.println("Distance    : " + distance + " km");
            System.out.println("Rate        : Rs." + rate + "/km");
            System.out.println("Total Fare  : Rs." + fare);

            // Cancellation before OTP
            System.out.println("\n--- Confirm Booking ---");
            System.out.println("1. Confirm & Share OTP");
            System.out.println("2. Cancel Ride");
            System.out.print("Enter your choice: ");

            String confInput = sc.nextLine().trim();
            int confChoice;
            try {
                confChoice = Integer.parseInt(confInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Returning to main menu.");
                continue;
            }

            if (confChoice == 2) {
                System.out.println("Ride cancelled successfully. No OTP was shared.");
                continue;
            }

            if (confChoice != 1) {
                System.out.println("Invalid choice! Returning to main menu.");
                continue;
            }

            // Generate and share 4-digit OTP
            int otp = 1000 + (int) (Math.random() * 9000);
            System.out.println("\nYour 4-digit OTP is: " + otp);
            System.out.println("OTP shared with you. The ride is now locked.");
            System.out.println("Once the OTP is shared, cancellation is NOT allowed.");
            System.out.println("Booking confirmed! Have a safe ride.");

            // Wait for user to press Enter before returning to main menu
            System.out.print("Press Enter to return to main menu...");
            sc.nextLine();
        }

        sc.close();
    }
}
