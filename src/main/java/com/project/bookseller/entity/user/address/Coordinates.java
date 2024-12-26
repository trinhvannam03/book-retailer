package com.project.bookseller.entity.user.address;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
public class Coordinates {
    private String longitude;
    private String latitude;
    public double getDoubleLongitude() {
        return Double.parseDouble(longitude);
    }
    public double getDoubleLatitude() {
        return Double.parseDouble(latitude);
    }
}
