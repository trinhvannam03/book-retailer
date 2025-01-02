package com.project.bookseller.dto.address;

import com.project.bookseller.entity.user.address.Country;
import lombok.Data;

@Data
public class CountryDTO {
    private long id;
    private String name;

    public static CountryDTO convertFromEntity(Country country) {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setId(country.getCountryId());
        countryDTO.setName(country.getCountryName());
        return countryDTO;
    }
}
