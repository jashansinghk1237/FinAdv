package com.finance.advisor.model;

public class PortfolioItem {
    private String stockSymbol;
    private double quantity;
    private double purchasePrice;

    public PortfolioItem(String stockSymbol, double quantity, double purchasePrice) {
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }

    // Getters and Setters
    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}

