package finaltica.model;

import java.util.Date;

public class Transaction {
    private int id;
    private int accountId;
    private String type; // "income" or "expense"
    private String category;
    private double amount;
    private String statement;
    private Date date;
    private String remark;

    public Transaction(int id, int accountId, String type, String category, double amount, String statement, Date date, String remark) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.statement = statement;
        this.date = date;
        this.remark = remark;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // Added setter for id
    public int getAccountId() { return accountId; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getStatement() { return statement; }
    public Date getDate() { return date; }
    public String getRemark() { return remark; }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", accountId=" + accountId + ", type='" + type + "', category='" + category +
                "', amount=" + amount + ", statement='" + statement + "', date=" + date + ", remark='" + remark + "'}";
    }
}