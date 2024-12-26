package com.project.bookseller.dto;

import com.project.bookseller.entity.location.Location;
import com.project.bookseller.entity.user.address.Coordinates;
import jakarta.persistence.Embedded;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationDTO {
    private long locationId;
    private String locationName;
    @Embedded
    private Coordinates coordinates;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private String detailedAddress;
    private CityDTO city;

    public static LocationDTO convertFromStore(Location location) {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setLocationId(location.getLocation_id());
        locationDTO.setLocationName(location.getLocationName());
        locationDTO.setCoordinates(location.getCoordinates());
        locationDTO.setDetailedAddress(location.getDetailedAddress());
        return locationDTO;
    }
}
