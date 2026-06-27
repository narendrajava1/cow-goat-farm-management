// AnimalStatus.java
package com.cgfms.animal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AnimalStatus {
    ACTIVE, SOLD, DECEASED, QUARANTINED;

    @JsonCreator
    public static AnimalStatus fromValue(String value) {
        for (AnimalStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Unknown animal status: " + value);
    }
}