package com.project.bookseller.dto.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.entity.user.UserAddress;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON
public class UserAddressDTO {
    private Long id;
    private String detailedAddress;
    private CityDTO city;
    private String phone;
    private StateDTO state;
    private CountryDTO country;
    private String fullName;
    private String fullAddress;
    private String longitude;
    private String latitude;

    public static UserAddressDTO convertFromEntity(UserAddress userAddress) {
        UserAddressDTO userAddressDTO = new UserAddressDTO();
        userAddressDTO.setId(userAddress.getUserAddressId());
        userAddressDTO.setDetailedAddress(userAddress.getDetailedAddress());
        userAddressDTO.setCity(CityDTO.convertFromEntity(userAddress.getCity()));
        userAddressDTO.setPhone(userAddress.getPhone());
        userAddressDTO.setFullName(userAddress.getFullName());
        userAddressDTO.setState(StateDTO.convertFromEntity(userAddress.getCity().getState()));
        userAddressDTO.setCountry(CountryDTO.convertFromEntity(userAddress.getCity().getState().getCountry()));
        userAddressDTO.setFullAddress(userAddress.getDetailedAddress() + ", " + userAddressDTO.getCity().getName() + ", " + userAddressDTO.getState().getName() + ", " + userAddressDTO.getCountry().getName());
        return userAddressDTO;
    }
}
