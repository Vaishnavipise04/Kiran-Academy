import java.util.Scanner;

public class BankingApp {

    static final double OPENING_BALANCE = 10000.0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // The single balance variable that every transaction updates
        double balance = OPENING_BALANCE;

        System.out.println("Welcome to Kiran Bank");
        System.out.println("Opening Balance: " + formatAmount(balance));

        while (true) {
            System.out.println("\n===== KIRAN BANK MENU =====");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            int choice = readChoice(sc);

            if (choice == 1) {
                System.out.println("Current Balance: " + formatAmount(balance));

            } else if (choice == 2) {
                System.out.print("Enter deposit amount: ");
                double amount = readAmount(sc);

                if (amount == -1) {
                    System.out.println("Transaction failed: Please enter a valid number.");
                    System.out.println("Current Balance: " + formatAmount(balance));
                } else if (amount <= 0) {
                    System.out.println("Transaction failed: Deposit amount must be greater than 0.");
                    System.out.println("Current Balance: " + formatAmount(balance));
                } else {
                    balance = balance + amount;
                    System.out.println("Deposit successful.");
                    System.out.println("Updated Balance: " + formatAmount(balance));
                }

            } else if (choice == 3) {
                System.out.print("Enter withdrawal amount: ");
                double amount = readAmount(sc);

                if (amount == -1) {
                    System.out.println("Transaction failed: Please enter a valid number.");
                    System.out.println("Current Balance: " + formatAmount(balance));
                } else if (amount <= 0) {
                    System.out.println("Transaction failed: Withdrawal amount must be greater than 0.");
                    System.out.println("Current Balance: " + formatAmount(balance));
                } else if (amount > balance) {
                    System.out.println("Transaction failed: Insufficient balance.");
                    System.out.println("Current Balance: " + formatAmount(balance));
                } else {
                    balance = balance - amount;
                    System.out.println("Withdrawal successful.");
                    System.out.println("Updated Balance: " + formatAmount(balance));
                }

            } else if (choice == 4) {
                System.out.println("Thank you for banking with us. Goodbye!");
                break;

            } else {
                System.out.println("Invalid choice! Please enter a number between 1 and 4.");
            }
        }

        sc.close();
    }

    // Reads the menu choice. Returns -1 when the user types something that is not a number.
    static int readChoice(Scanner sc) {
        String input = sc.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Reads a transaction amount. Returns -1 when the user types something that is not a number.
    static double readAmount(Scanner sc) {
        String input = sc.nextLine().trim();
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Prints every amount in the same format, for example Rs.10000.00
    static String formatAmount(double amount) {
        return "Rs." + String.format("%.2f", amount);
    }
}
