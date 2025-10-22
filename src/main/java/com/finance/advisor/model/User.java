// package com.finance.advisor.model;

// import java.util.ArrayList;
// import java.util.List;

// import org.springframework.data.annotation.Id;
// import org.springframework.data.mongodb.core.mapping.Document;

// @Document(collection = "users")  //yo spring nu dss rehya ki bai user vasta yo kam sara



// public class User {
//     @Id // te yo ek roll no type unique id deva ga
//     private String id;
//     private String username;
//     private String password;
//     private List<String> roles = new ArrayList<>();
//     private List<Expense> expenses = new ArrayList<>();
//     private List<Income> incomeSources = new ArrayList<>(); // income sourse bate ga catgry
//     private List<PortfolioItem> portfolio = new ArrayList<>();
//     private double balance = 10000.00; // demo money hai fixed

// // getter setter oohi same ne sare

//     // Getters and Setters
//     public String getId() {
//         return id;
//     }

//     public void setId(String id) {
//         this.id = id;
//     }

//     public String getUsername() {
//         return username;
//     }

//     public void setUsername(String username) {
//         this.username = username;
//     }

//     public String getPassword() {
//         return password;
//     }

//     public void setPassword(String password) {
//         this.password = password;
//     }

//     public List<String> getRoles() {
//         return roles;
//     }

//     public void setRoles(List<String> roles) {
//         this.roles = roles;
//     }

//     public List<Expense> getExpenses() {
//         return expenses;
//     }

//     public void setExpenses(List<Expense> expenses) {
//         this.expenses = expenses;
//     }

//     public List<PortfolioItem> getPortfolio() {
//         return portfolio;
//     }

//     public void setPortfolio(List<PortfolioItem> portfolio) {
//         this.portfolio = portfolio;
//     }

//     public double getBalance() {
//         return balance;
//     }

//     public void setBalance(double balance) {
//         this.balance = balance;
//     }

//     public List<Income> getIncomeSources() {
//         return incomeSources;
//     }

//     public void setIncomeSources(List<Income> incomeSources) {
//         this.incomeSources = incomeSources;
//     }
// }



























































package com.finance.advisor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private List<String> roles = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();
    private List<Income> incomeSources = new ArrayList<>();
    private List<PortfolioItem> portfolio = new ArrayList<>();
    private double balance = 10000.00;

    // --- NEW FIELDS FOR ADMIN CONTROL ---
    private boolean blocked = false;
    private double fineAmount = 0.0;
    private String fineReason = null;
    // --- END OF NEW FIELDS ---

    // Getters and Setters for all fields...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<Expense> getExpenses() { return expenses; }
    public void setExpenses(List<Expense> expenses) { this.expenses = expenses; }
    public List<Income> getIncomeSources() { return incomeSources; }
    public void setIncomeSources(List<Income> incomeSources) { this.incomeSources = incomeSources; }
    public List<PortfolioItem> getPortfolio() { return portfolio; }
    public void setPortfolio(List<PortfolioItem> portfolio) { this.portfolio = portfolio; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public String getFineReason() { return fineReason; }
    public void setFineReason(String fineReason) { this.fineReason = fineReason; }
}

