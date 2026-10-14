import java.util.Scanner;

public class StudentMarks {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter student name: ");
        String name = sc.nextLine();

        System.out.println("Enter marks for 5 subjects:");
        int m1 = sc.nextInt();
        int m2 = sc.nextInt();
        int m3 = sc.nextInt();
        int m4 = sc.nextInt();
        int m5 = sc.nextInt();

        int total = m1 + m2 + m3 + m4 + m5;
        double percentage = total / 5.0;

        System.out.println("\nStudent Name: " + name);
        System.out.println("Total Marks: " + total + "/500");
        System.out.println("Percentage: " + percentage + "%");

        if (m1 >= 35 && m2 >= 35 && m3 >= 35
                && m4 >= 35 && m5 >= 35) {
            System.out.println("Result: Pass");
        } else {
            System.out.println("Result: Fail");
        }

        sc.close();
    }
}
