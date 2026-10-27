package com.example.application.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public JwtAuthenticationToken(UserDetails userDetails) {
        super(userDetails, null, userDetails.getAuthorities());
    }
}
