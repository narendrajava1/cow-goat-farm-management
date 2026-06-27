package com.cgfms.animal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

// Gender.java
public enum Gender {
    MALE, FEMALE, UNKNOWN;

    @JsonCreator
    public static Gender fromValue(String value) {
        for (Gender g : values()) {
            if (g.name().equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + value);
    }
}