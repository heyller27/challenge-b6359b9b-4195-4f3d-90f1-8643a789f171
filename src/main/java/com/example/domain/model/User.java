package com.example.domain.model;

import java.util.List;

public record User(Long id, String username, String password, List<String> roles) {}
