package com.finance.advisor.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.advisor.model.User;
import com.finance.advisor.repository.UserRepository;
import com.finance.advisor.security.JwtTokenProvider;












// Spring ko batata hai ki yeh class web requests handle karegi 
// aur response ko  (directly) JSON format mein bhejegi.

// Class ki Pehchaan (Receptionist ki Desk)
//  adhar card hai bro

@RestController
@RequestMapping("/api/auth")
//  Is controller ke saare URLs /api/auth se shuru honge.
public class AuthController {

    private final AuthenticationManager authenticationManager;  //"Head of Security"  Col. JAM Sir
    private final UserRepository userRepository;    //database
    private final PasswordEncoder passwordEncoder;   // password ko simple se masala daar bana deti h
    private final JwtTokenProvider jwtTokenProvider;    //ID Card   dega jo ek session k leye he hoga
    //security ++

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login") 
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password"))
        );
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final String token = jwtTokenProvider.generateToken(userDetails);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/signup")  // post req bheja ga
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> signUpRequest) {
        if (userRepository.findByUsername(signUpRequest.get("username")).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken!"));
        }
//else
        User user = new User();
        user.setUsername(signUpRequest.get("username"));
        user.setPassword(passwordEncoder.encode(signUpRequest.get("password")));
        user.setRoles(Collections.singletonList("ROLE_USER")); // Default role

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }
}

