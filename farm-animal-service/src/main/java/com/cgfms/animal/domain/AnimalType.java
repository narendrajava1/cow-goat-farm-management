package com.cgfms.animal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

// AnimalType.java
public enum AnimalType {
    COW, GOAT;

    @JsonCreator
    public static AnimalType fromValue(String value) {
        for (AnimalType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown animal type: " + value);
    }
}