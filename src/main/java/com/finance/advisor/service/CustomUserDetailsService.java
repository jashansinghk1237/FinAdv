
package com.finance.advisor.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.finance.advisor.model.User;
import com.finance.advisor.repository.UserRepository;





@Service     // yh pechaan patar hai spring k leye

public class CustomUserDetailsService implements UserDetailsService {
    // promise the spring ki username dega

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
        // yh function [class] ==> record room se data fetch krege
    }

    @Override
    // yh  authController ko bata deta h ki bhai yh banda login kr rha h krne do
    // jese gate par GATE-PASS check hota h 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        // spring k leye id card bana deta h
        // username+ps+role  yh declare krta h
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), 
                user.getPassword(), 
                getAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        // This helper method converts roles to the correct format.
        //  yar yh jo spring hai na 
        // yh angrez hai esko  humari language nahi ati
        // thats y we use this
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }
}


