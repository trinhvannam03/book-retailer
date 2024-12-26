package com.project.bookseller.controller;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.dto.AuthDTO;
import com.project.bookseller.dto.OrderInformationDTO;
import com.project.bookseller.dto.UserAddressDTO;
import com.project.bookseller.dto.UserDTO;
import com.project.bookseller.entity.user.Session;
import com.project.bookseller.exceptions.PassWordNotMatch;
import com.project.bookseller.service.OrderService;
import com.project.bookseller.service.UserAddressService;
import com.project.bookseller.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RestController("/")
@RequestMapping("/api/user")

public class UserController {
    private final UserAddressService userAddressService;
    private final UserService userService;
    private final OrderService orderService;

    //get addresses
    @GetMapping("/address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserAddressDTO>> getUserAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserAddressDTO> userAddressDTOs = userAddressService.findUserAddresses(userDetails);
        return new ResponseEntity<>(userAddressDTOs, HttpStatusCode.valueOf(200));
    }

    //get basic info
    @PreAuthorize("hasRole('ROLE_USER') and isAuthenticated()")
    @GetMapping("/profile")
    ResponseEntity<UserDTO> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }
        UserDTO userDTO = userService.getUserProfile(userDetails);
        return new ResponseEntity<>(userDTO, HttpStatusCode.valueOf(200));
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PutMapping("/profile")
    ResponseEntity<UserDTO> updateUserProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDTO userDTO) {
        UserDTO userDTOUpdated = userService.updateUserProfile(userDetails, userDTO);
        return new ResponseEntity<>(userDTOUpdated, HttpStatusCode.valueOf(200));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/orders")
    ResponseEntity<List<OrderInformationDTO>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }
        List<OrderInformationDTO> orders = orderService.getUserOrders(userDetails);
        return new ResponseEntity<>(orders, HttpStatusCode.valueOf(200));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/password")
    ResponseEntity<Map<String, Object>> changePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody AuthDTO credentials) {
        try {
            Map<String, Object> result = userService.changePassword(userDetails, credentials);
            return new ResponseEntity<>(result, HttpStatusCode.valueOf(200));
        } catch (PassWordNotMatch e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sessions")
    ResponseEntity<Set<Session>> getUserSessions(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }
        Set<Session> sessions = userService.getSessions(userDetails);
        return new ResponseEntity<>(sessions, HttpStatusCode.valueOf(200));
    }
}
