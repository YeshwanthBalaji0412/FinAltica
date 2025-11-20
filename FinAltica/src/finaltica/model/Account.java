package finaltica.model;

public class Account {
    private int id;
    private int userId;
    private String name;
    private double balance;
    private double totalExpenses;

    public Account(int id, int userId, String name, double balance, double totalExpenses) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.totalExpenses = totalExpenses;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public double getTotalExpenses() { return totalExpenses; }

    @Override
    public String toString() {
        return "Account{id=" + id + ", userId=" + userId + ", name='" + name + "', balance=" + balance + ", totalExpenses=" + totalExpenses + "}";
    }
}