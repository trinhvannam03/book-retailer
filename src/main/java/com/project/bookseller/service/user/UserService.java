package com.project.bookseller.service.user;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.dto.address.UserAddressDTO;
import com.project.bookseller.dto.auth.AuthDTO;
import com.project.bookseller.dto.auth.RegisterDTO;
import com.project.bookseller.dto.UserDTO;
import com.project.bookseller.entity.user.Session;
import com.project.bookseller.entity.user.User;
import com.project.bookseller.entity.user.UserAddress;
import com.project.bookseller.exceptions.PassWordNotMatch;
import com.project.bookseller.repository.OrderInformationRepository;
import com.project.bookseller.repository.UserAddressRepository;
import com.project.bookseller.repository.UserRepository;
import com.project.bookseller.service.auth.SessionService;
import com.project.bookseller.service.auth.TokenService;
import com.project.bookseller.service.auth.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OrderInformationRepository orderInformationRepository;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final SessionService sessionService;
    private final UserAddressRepository userAddressRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDTO register(RegisterDTO info) throws PassWordNotMatch {
        if (!info.getConfirmedPassword().equals(info.getPassword())) {
            throw new PassWordNotMatch("confirmedPassword", "Confirmed Password Must Match");
        }
        User user = new User();
        user.setEmail(info.getEmail());
        user.setPasswordHash(passwordEncoder.encode(info.getPassword()));
        user.setFullName(info.getFullName());
        userRepository.save(user);
        return UserDTO.convertFromEntity(user);
    }

    public Map<String, String> logout(UserDetails userDetails, Session session) {
        sessionService.deleteSession(userDetails.getUserId(), session);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logout Successful");
        return response;
    }

    public UserDTO login(AuthDTO credentials) throws InvalidCredentialsException {
        String password = credentials.getPassword();
        String identifier = credentials.getIdentifier();
        if (password == null || identifier == null) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }
        UserDetails userDetails = userDetailsService.loadUserByIdentifier(identifier);
        if (userDetails != null && passwordEncoder.matches(password, userDetails.getPasswordHash())) {
            UserDTO userDTO = UserDTO.convertFromEntity(userDetails.getUser());
            Session session = sessionService.createSession(userDetails.getUser());
            String accessToken = tokenService.generateAccessToken(userDetails, session.getSessionId());
            String refreshToken = tokenService.generateRefreshToken(userDetails, session.getSessionId());
            userDTO.setAccessToken(accessToken);
            userDTO.setRefreshToken(refreshToken);
            session.setBrowserName(credentials.getBrowserName());
            session.setDeviceType(credentials.getDeviceType());
            session.setOsName(credentials.getOsName());
            session.setUserAgent(credentials.getUserAgent());
            session.setOsVersion(credentials.getOsVersion());
            session.setIpAddress(credentials.getIpAddress());
            Set<Session> sessions = sessionService.addSession(userDetails.getUserId(), session);
            userDTO.setSession(session);
            userDTO.setSessions(sessions);
            return userDTO;
        }
        throw new InvalidCredentialsException("Invalid Credentials");
    }

    public UserDTO getUserProfile(UserDetails userDetails) {
        User user = userDetails.getUser();
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setFullName(user.getFullName());
        userDTO.setUserTier(user.getUserTier());
        userDTO.setPhone(user.getPhone());
        userDTO.setRoleName(user.getRoleName());
        userDTO.setGender(user.getGender());
        userDTO.setDateOfBirth(user.getDateOfBirth());
        userDTO.setProfilePicture(user.getProfilePicture());
        return userDTO;
    }

    public Set<Session> getSessions(UserDetails userDetails) {
        Set<Session> sessions = sessionService.getSessions(userDetails.getUserId());
        for (Session session : sessions) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");
            String sessionDescription = String.format(
                    "Created at %s on %s, IP address %s, Browser  %s",
                    session.getCreatedAt().format(formatter),
                    session.getBrowserName(),
                    session.getIpAddress(),
                    session.getUserAgent()
            );
            session.setSessionDescription(sessionDescription);
        }
        Set<Session> sortedSessions = new TreeSet<>(sessions).descendingSet();
        return sortedSessions;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDTO updateUserProfile(UserDetails userDetails, UserDTO userDTO) {
        try {
            User user = userDetails.getUser();
            user.setFullName(userDTO.getFullName());
            user.setEmail(userDTO.getEmail());
            user.setPhone(userDTO.getPhone());
            user.setGender(userDTO.getGender());
            user.setProfilePicture(userDTO.getProfilePicture());
            user.setDateOfBirth(userDTO.getDateOfBirth());
            userRepository.save(user);
            return userDTO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> changePassword(UserDetails userDetails, AuthDTO credentials) throws PassWordNotMatch {
        String currentPassword = credentials.getCurrentPassword();
        String newPassword = credentials.getPassword();
        String confirmedPassword = credentials.getConfirmedPassword();
        String passwordHash = userDetails.getPasswordHash();
        if (!currentPassword.isEmpty() && !newPassword.isEmpty() && !confirmedPassword.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            if (newPassword.equals(confirmedPassword) && passwordEncoder.matches(currentPassword, passwordHash)) {
                User user = userDetails.getUser();
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                result.put("message", "Password Changed");
                return result;
            }
        }
        throw new PassWordNotMatch("message", "Passwords mismatch!");
    }

    public List<UserAddressDTO> findUserAddresses(UserDetails userDetails) {
        List<UserAddress> userAddresses = userAddressRepository.findUserAddressesByUserId(userDetails.getUser().getUserId());
        List<UserAddressDTO> userAddressDTOs = new ArrayList<>();
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
