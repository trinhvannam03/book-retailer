package com.project.bookseller.service;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.dto.UserAddressDTO;
import com.project.bookseller.entity.user.UserAddress;
import com.project.bookseller.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressService {
    private final UserAddressRepository userAddressRepository;

    public List<UserAddressDTO> findUserAddresses(UserDetails userDetails) {
        List<UserAddress> userAddresses = userAddressRepository.findUserAddressesByUserId(userDetails.getUser().getUserId());
        List<UserAddressDTO> userAddressDTOs = new ArrayList<>();
        for (UserAddress userAddress : userAddresses) {
            UserAddressDTO userAddressDTO = UserAddressDTO.convertFromUserAddress(userAddress);
            userAddressDTOs.add(userAddressDTO);
        }
        return userAddressDTOs;
    }
}
