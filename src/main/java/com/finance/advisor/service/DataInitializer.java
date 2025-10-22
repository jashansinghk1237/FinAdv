package com.finance.advisor.service;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.finance.advisor.model.User;
import com.finance.advisor.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    //spring ko promise kre ga ki hum ek RUNNER function dega

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        //Check Karna ki Admin Pehle se Hai ya Nahi
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            // --- THIS IS THE CRITICAL CHANGE ---
            admin.setRoles(List.of("ROLE_ADMIN")); // Was "ADMIN", now "ROLE_ADMIN"
            // ------------------------------------
            admin.setBalance(100000.0); // Give admin some default balance
            userRepository.save(admin);
            System.out.println(">>>> CREATED DEFAULT ADMIN USER <<<<");
        }
    }
}

