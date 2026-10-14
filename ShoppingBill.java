public class ShoppingBill {

    static double calculateElectronicsGST(double a, double b) {
        return (a + b) + ((a + b) * 18 / 100);
    }

    static double calculateFashionGST(double c, double d) {
        return (c + d) + ((c + d) * 13 / 100);
    }

    static double calculateFoodGST(double e) {
        return e + (e * 7 / 100);
    }

    static double totalBill(double a, double b, double c, double d, double e) {
        return calculateElectronicsGST(a, b)
                + calculateFashionGST(c, d)
                + calculateFoodGST(e);
    }

    static double calculateDiscountedBill(double a, double b, double c, double d, double e) {
        double total = totalBill(a, b, c, d, e);
        double discount = total * 20 / 100;
        return total - discount;
    }

    public static void main(String[] args) {
        double a = 100, b = 100, c = 100, d = 100, e = 100;

        double total = totalBill(a, b, c, d, e);
        double finalAmount = calculateDiscountedBill(a, b, c, d, e);
        double discount = total - finalAmount;

        System.out.printf("Total after GST = %.2f | Discount = %.2f | Final payable amount = %.2f%n",
                total, discount, finalAmount);
    }
}
