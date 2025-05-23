import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import com.google.gson.Gson;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.opencsv.CSVReader;

// Main POS System Class - it handles all main operations
public class SuperSaverPOS {
    private ShopData data; // store shop data items
    private List<Bill> allbills; // all bills stored here
    private List<HoldBill> holdbills; // Hold bills stored

    // The constructor will load the data from a file and initialize the bills lists
    public SuperSaverPOS(String dataFile) {
        data = new ShopData(dataFile);
        allbills = new ArrayList<>();
        holdbills = new ArrayList<>();
    }

    // Start the POS system and show the menu for the user to pick an option
    public void start() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Super-Saving POS ===");
            System.out.println("1. New Bill Process"); // for creating new bill
            System.out.println("2. Handle Hold Bill"); // for processing hold bill
            System.out.println("3. Show Revenue Report"); // show total earnings
            System.out.println("4. Exit"); // close program
            System.out.print("Pick option: ");
            int choice = scan.nextInt();
            scan.nextLine();

            switch (choice) {
                case 1:
                    newBill(scan); // call method to create bill
                    break;
                case 2:
                    handleHoldBill(scan); // call method for hold bill
                    break;
                case 3:
                    showReport(); // create report of sales
                    break;
                case 4:
                    System.out.println("Closing POS System...");
                    return;
                default:
                    System.out.println("Wrong choice. Try again.");
            }
        }
    }

    // This method handles the creation of a new bill
    private void newBill(Scanner scan) {
        System.out.print("Enter Cashier: ");
        String cashier = scan.nextLine();
        System.out.print("Enter Branch: ");
        String location = scan.nextLine();
        System.out.print("Enter Customer: ");
        String buyer = scan.nextLine();

        Bill bill = new Bill(cashier, location, buyer);

        while (true) {
            System.out.print("Enter Item Code (or 'done' to stop): ");
            String code = scan.nextLine();
            if (code.equalsIgnoreCase("done")) break; // stop when user enters "done"

            Product item = data.getItem(code);
            if (item == null) {
                System.out.println("Item not found.");
                continue; // if item not found, ask again
            }

            System.out.print("Enter Amount: ");
            int amount = scan.nextInt();
            scan.nextLine();
            bill.addItem(item, amount);
        }

        // Apply discount
        System.out.print("Enter Discount Percentage (0 for no discount): ");
        double discount = scan.nextDouble();
        scan.nextLine();
        bill.applyDiscount(discount);

        bill.makePDF("bill.pdf"); // save bill as PDF
        allbills.add(bill);
        System.out.println("Bill saved successfully.");
    }

    // This method handles loading hold bills for later processing
    private void handleHoldBill(Scanner scan) {
        System.out.print("Enter hold bill file path: ");
        String path = scan.nextLine();
        HoldBill holdBill = HoldBill.getHoldBill(path);
        if (holdBill == null) {
            System.out.println("Failed to retrieve hold bill.");
        } else {
            System.out.println("Hold bill loaded.");
        }
    }

    // This method shows the total revenue report
    private void showReport() {
        Report report = new Report();
        report.createReport(allbills);
    }
}

// Database Class - it loads item details from file
class ShopData {
    private Map<String, Product> stock; // map to hold product info

    // This constructor loads the item data from a CSV file
    public ShopData(String filePath) {
        stock = new HashMap<>();
        loadCSV(filePath);
    }

    // This method loads the item data from a CSV file
    private void loadCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                stock.put(line[0], new Product(line[0], Double.parseDouble(line[1])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This method returns a product based on the code
    public Product getItem(String code) {
        return stock.get(code);
    }
}

// Bill Class - store bill data
class Bill {
    private String cashier, shop, buyer;
    private List<Product> items;
    private double discountTotal, costTotal;
    private double discountPercent; // Discount percentage

    // The Bill constructor sets up the bill with cashier, shop and buyer details
    public Bill(String cashier, String shop, String buyer) {
        this.cashier = cashier;
        this.shop = shop;
        this.buyer = buyer;
        this.items = new ArrayList<>();
        this.discountPercent = 0.0; // Default no discount
    }

    // This method adds an item to the bill
    public void addItem(Product item, int amount) {
        items.add(item);
        costTotal += item.getPrice() * amount;
    }

    // This method applies a discount to the bill
    public void applyDiscount(double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            System.out.println("Invalid discount. Must be between 0-100%.");
            return;
        }
        this.discountPercent = discountPercent;
        this.discountTotal = (costTotal * discountPercent) / 100;
    }

    // This method generates a PDF of the bill
    public void makePDF(String filePath) {
        try (PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf)) {
            doc.add(new Paragraph("Cashier: " + cashier));
            doc.add(new Paragraph("Shop: " + shop));
            doc.add(new Paragraph("Buyer: " + buyer));
            doc.add(new Paragraph("Cost Total: " + costTotal));
            doc.add(new Paragraph("Discount Applied: " + discountPercent + "%"));
            doc.add(new Paragraph("Final Amount: " + (costTotal - discountTotal)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Product Class - store product information
class Product {
    private String code;
    private double price;

    // Product constructor stores the product code and price
    public Product(String code, double price) {
        this.code = code;
        this.price = price;
    }

    // This method returns the price of the product
    public double getPrice() {
        return price;
    }
}

// Report Class - create reports
class Report {
    // This method generates a report showing total earnings
    public void createReport(List<Bill> bills) {
        double totalEarned = bills.stream().mapToDouble(b -> b.costTotal).sum();
        System.out.println("Total Earned: " + totalEarned);
    }
}

// Hold Bill Class - hold bill data for later
class HoldBill {
    private Bill bill;

    // This method saves the hold bill data to a file
    public void saveHoldBill(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            new Gson().toJson(this, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This method loads the hold bill data from a file
    public static HoldBill getHoldBill(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            return new Gson().fromJson(reader, HoldBill.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

// Main Run Class
class SuperSaverPOSGroup_CodingThunders {
    public static void main(String[] args) {
        SuperSaverPOS pos = new SuperSaverPOS("items.csv");
        pos.start();
    }
}