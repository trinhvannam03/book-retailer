package com.project.bookseller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.entity.user.UserRole;
import com.project.bookseller.entity.user.UserTier;
import com.project.bookseller.entity.user.Gender;
import com.project.bookseller.entity.user.Session;
import com.project.bookseller.entity.user.User;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO implements Serializable {
    private long userId;
    private String email;
    private String phone;
    private String fullName;
    private String profilePicture;
    private String passwordHash;
    private UserTier userTier;
    private UserRole roleName;
    private Gender gender;
    private Date dateOfBirth;
    private String accessToken;
    private String refreshToken;
    Set<Session> sessions = new HashSet<>();
    private Session session;

    public static UserDTO convertFromEntity(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setFullName(user.getFullName());
        userDTO.setUserTier(user.getUserTier());
        userDTO.setGender(user.getGender());
        userDTO.setPhone(user.getPhone());
        userDTO.setRoleName(user.getRoleName());
        userDTO.setProfilePicture(user.getProfilePicture());
        userDTO.setDateOfBirth(user.getDateOfBirth());
        userDTO.setUserId(user.getUserId());
        return userDTO;
    }
}
