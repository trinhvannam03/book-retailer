package com.project.bookseller.service;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.dto.UserAddressDTO;
import com.project.bookseller.entity.user.UserAddress;
import com.project.bookseller.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final UserAddressRepository userAddressRepository;

    @Transactional
    public List<UserAddressDTO> findAddresses(UserDetails userDetails) {
        List<UserAddressDTO> userAddressDTOs = new ArrayList<>();
        List<UserAddress> userAddresses = userAddressRepository.findUserAddressesByUserId(userDetails.getUserId());
        for (UserAddress userAddress : userAddresses) {
            UserAddressDTO userAddressDTO = UserAddressDTO.convertFromUserAddress(userAddress);
            userAddressDTOs.add(userAddressDTO);
        }
        return userAddressDTOs;
    }

    @Transactional
    public UserAddressDTO createAddress(UserDetails userDetails, UserAddressDTO userAddressDTO) {
        UserAddress userAddress = new UserAddress();
        userAddress.getCity().setCityName(userAddressDTO.getCity().getName());
        userAddress.getCity().setCityId(userAddressDTO.getCity().getId());
        userAddress.setFullName(userAddressDTO.getFullName());
        userAddress.setPhone(userAddressDTO.getPhone());
        userAddress.setDetailedAddress(userAddressDTO.getDetailedAddress());
        userAddress.setUser(userDetails.getUser());
        userAddressRepository.save(userAddress);
        userAddressDTO.setId(userAddress.getUserAddressId());
        return userAddressDTO;
    }
}
