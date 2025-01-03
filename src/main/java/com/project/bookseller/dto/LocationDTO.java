package com.project.bookseller.dto;

import com.project.bookseller.dto.address.CityDTO;
import com.project.bookseller.dto.address.StateDTO;
import com.project.bookseller.entity.location.Location;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationDTO {
    private long locationId;
    private String locationName;
    private String longitude;
    private String latitude;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private String fullAddress;
    private String detailedAddress;
    private CityDTO city;
    private StateDTO state;

    public static LocationDTO convertFromEntity(Location location) {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setLocationId(location.getLocationId());
        locationDTO.setLocationName(location.getLocationName());
        locationDTO.setCity(CityDTO.convertFromEntity(location.getCity()));
        return locationDTO;
    }
}
