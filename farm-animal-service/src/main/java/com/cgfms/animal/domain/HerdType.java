// HerdType.java
package com.cgfms.animal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum HerdType {
    MILKING, BREEDING, CALF, OTHER;

    @JsonCreator
    public static HerdType fromValue(String value) {
        for (HerdType t : values()) {
            if (t.name().equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Unknown herd type: " + value);
    }
}