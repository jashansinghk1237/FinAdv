package com.finance.advisor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.finance.advisor.model.User;
import com.finance.advisor.repository.UserRepository;

@Service
public class StockService {
    private final UserRepository userRepository;
    private final Map<String, Double> stockPrices = new HashMap<>();
    private final String[] stockSymbols = {
        "GOOGL", "AAPL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "JPM", "V", "JNJ",
        "WMT", "PG", "UNH", "HD", "MA", "BAC", "DIS", "PYPL", "ADBE", "NFLX",
        "CRM", "KO", "PFE", "CSCO", "PEP", "XOM", "T", "INTC", "CMCSA", "VZ",
        "ABT", "NKE", "MCD", "MDT", "COST", "LLY", "ABBV", "DHR", "TMO", "AVGO",
        "TXN", "HON", "QCOM", "UNP", "SBUX", "CAT", "LOW", "IBM", "GS", "BA",
        "GE", "MMM", "AXP", "RTX", "CVS", "DE", "PLD", "BLK", "AMT", "SPGI",
        "NOW", "UPS", "LMT", "NEE", "F", "GM", "UBER", "ZM", "SQ", "SHOP",
        "SNOW", "ROKU", "SPOT", "TWLO", "ETSY", "PINS", "CHWY", "W", "COIN", "RBLX",
        "AAL", "DAL", "UAL", "LUV", "MAR", "HLT", "BKNG", "EXPE", "CCL", "RCL",
        "NCLH", "SIRI", "AMC", "GME", "BB", "NOK", "PLTR", "SOFI", "HOOD", "AFRM"
    };

    public StockService(UserRepository userRepository) {
        this.userRepository = userRepository;
        Random random = new Random();
        for (String symbol : stockSymbols) {
            stockPrices.put(symbol, 50 + (450 * random.nextDouble())); 
            // 50 and 500 k bech me rhna chaiye $$$$
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(String id, String username, double balance) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(username);
        user.setBalance(balance);
        return userRepository.save(user);
    }

    public Map<String, Double> getAllStockPrices() {
        return new HashMap<>(stockPrices);
    }

    public void updateStockPrices() {
        Random random = new Random();
        for (String symbol : stockPrices.keySet()) {
            double currentPrice = stockPrices.get(symbol);
            double changePercent = (random.nextDouble() - 0.49) * 0.1; 
            double newPrice = currentPrice * (1 + changePercent);
            stockPrices.put(symbol, Math.max(newPrice, 1.0)); 
        }
    }
}

