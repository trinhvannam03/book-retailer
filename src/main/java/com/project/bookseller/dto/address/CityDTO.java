package com.project.bookseller.dto.address;

import com.project.bookseller.entity.user.address.City;
import lombok.Data;

@Data
public class CityDTO {
    private long id;
    private String name;

    public static CityDTO convertFromEntity(City city) {
        CityDTO cityDTO = new CityDTO();
        cityDTO.id = city.getCityId();
        cityDTO.name = city.getCityName();
        return cityDTO;
    }
}
