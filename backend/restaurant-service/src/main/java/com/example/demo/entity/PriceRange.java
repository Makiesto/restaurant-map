package com.example.demo.entity;

import lombok.Getter;

@Getter
public enum PriceRange {
    BUDGET("$"),
    MODERATE("$$"),
    EXPENSIVE("$$$"),
    LUXURY("$$$$");

    private final String symbol;

    PriceRange(String symbol) {
        this.symbol = symbol;
    }

    // Helper method to convert from frontend string values
    public static PriceRange fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return PriceRange.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}