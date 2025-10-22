package com.finance.advisor.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class SchedulingService {
    // Thread safety ke liye AtomicLong use kar rahe hain
    private final AtomicLong rateInMilliseconds = new AtomicLong(5000); // Default 5 seconds

    public void setRateInSeconds(long seconds) {
        this.rateInMilliseconds.set(seconds * 1000);
    }

    public long getRateInMilliseconds() {
        return this.rateInMilliseconds.get();
    }
}




// Sochiye, ek hi samay par AdminController is time ko badal kar 3000 karne ki koshish kar raha hai, 
// aur theek usi samay StockUpdateScheduler is time ko padhne ki koshish kar raha hai. Aise mein data corrupt ho sakta hai