package com.finance.advisor.controller;

import com.finance.advisor.model.Expense;
import com.finance.advisor.model.Income;
import com.finance.advisor.model.PortfolioItem;
import com.finance.advisor.model.User;
import com.finance.advisor.repository.UserRepository;
import com.finance.advisor.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final StockService stockService;

    public UserController(UserRepository userRepository, StockService stockService) {
        this.userRepository = userRepository;
        this.stockService = stockService;
    }

    private User getUserByDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(getUserByDetails(userDetails));
    }

    @PostMapping("/expenses")
    public ResponseEntity<User> addExpense(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> expenseRequest) {
        User user = getUserByDetails(userDetails);
        String category = expenseRequest.get("category");
        double amount = Double.parseDouble(expenseRequest.get("amount"));
        
        // Subtract from balance
        user.setBalance(user.getBalance() - amount);
        user.getExpenses().add(new Expense(category, amount, LocalDate.now()));
        userRepository.save(user);
        
        return ResponseEntity.ok(user); // Return updated user
    }
    
    @PostMapping("/income")
    public ResponseEntity<User> addIncome(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> incomeRequest) {
        User user = getUserByDetails(userDetails);
        String source = incomeRequest.get("source");
        double amount = Double.parseDouble(incomeRequest.get("amount"));
        
        // Add to balance
        user.setBalance(user.getBalance() + amount);
        user.getIncomeSources().add(new Income(source, amount, LocalDate.now()));
        userRepository.save(user);
        
        return ResponseEntity.ok(user); // Return updated user
    }

    @PostMapping("/portfolio/buy")
    public ResponseEntity<?> buyStock(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> buyRequest) {
        User user = getUserByDetails(userDetails);
        String stockSymbol = buyRequest.get("stockSymbol");
        double quantity = Double.parseDouble(buyRequest.get("quantity"));
        
        Double currentPrice = stockService.getAllStockPrices().get(stockSymbol);
        if (currentPrice == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Stock symbol not found."));
        }

        double totalCost = currentPrice * quantity;
        if (user.getBalance() < totalCost) {
            return ResponseEntity.badRequest().body(Map.of("message", "Insufficient balance."));
        }

        user.setBalance(user.getBalance() - totalCost);
        
        PortfolioItem existingItem = user.getPortfolio().stream()
            .filter(item -> item.getStockSymbol().equals(stockSymbol))
            .findFirst()
            .orElse(null);

        if (existingItem != null) {
            double newQuantity = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newQuantity);
        } else {
            user.getPortfolio().add(new PortfolioItem(stockSymbol, quantity, currentPrice));
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/portfolio/sell")
    public ResponseEntity<?> sellStock(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> sellRequest) {
        User user = getUserByDetails(userDetails);
        String stockSymbol = sellRequest.get("stockSymbol");
        double quantityToSell = Double.parseDouble(sellRequest.get("quantity"));

        PortfolioItem stockToSell = user.getPortfolio().stream()
            .filter(item -> item.getStockSymbol().equals(stockSymbol))
            .findFirst()
            .orElse(null);

        if (stockToSell == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "You do not own this stock."));
        }

        if (stockToSell.getQuantity() < quantityToSell) {
            return ResponseEntity.badRequest().body(Map.of("message", "You do not have enough shares to sell."));
        }

        Double currentPrice = stockService.getAllStockPrices().get(stockSymbol);
        if (currentPrice == null) {
            return ResponseEntity.status(500).body(Map.of("message", "Could not fetch current price for the stock."));
        }

        double totalValue = currentPrice * quantityToSell;
        user.setBalance(user.getBalance() + totalValue);

        // Update portfolio by reducing quantity or removing the item
        stockToSell.setQuantity(stockToSell.getQuantity() - quantityToSell);
        if (stockToSell.getQuantity() < 0.0001) { // Use a small threshold for floating point comparison
            user.getPortfolio().remove(stockToSell);
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}

