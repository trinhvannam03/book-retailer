package com.project.bookseller.service.user;

import com.project.bookseller.authentication.AuthenticationResponseDTO;
import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.dto.GoogleResponse;
import com.project.bookseller.dto.UserDTO;
import com.project.bookseller.dto.address.UserAddressDTO;
import com.project.bookseller.dto.auth.AuthDTO;
import com.project.bookseller.dto.auth.RegisterDTO;
import com.project.bookseller.entity.user.Session;
import com.project.bookseller.entity.user.User;
import com.project.bookseller.entity.user.UserAddress;
import com.project.bookseller.exceptions.BadCredentialsException;
import com.project.bookseller.exceptions.PassWordNotMatch;
import com.project.bookseller.exceptions.UniqueColumnViolationException;
import com.project.bookseller.repository.OrderRepository;
import com.project.bookseller.repository.UserAddressRepository;
import com.project.bookseller.repository.UserRepository;
import com.project.bookseller.service.auth.SessionService;
import com.project.bookseller.service.auth.TokenService;
import com.project.bookseller.service.auth.UserPrincipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OrderRepository orderInformationRepository;
    private final UserPrincipalService userPrincipalService;
    private final TokenService tokenService;
    private final SessionService sessionService;
    private final UserAddressRepository userAddressRepository;
    private final RestTemplate restTemplate;
    private static final String CLIENT_SECRET = "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";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String REDIRECT_URI = "http://localhost:3000/auth/callback";
    private static final String USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String CLIENT_ID = "315074024599-2ftepktk2pkjo21bikn8nj0nvav618tr.apps.googleusercontent.com";

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AuthenticationResponseDTO register(RegisterDTO info) throws PassWordNotMatch {
        if (!info.getConfirmedPassword().equals(info.getPassword())) {
            throw new PassWordNotMatch("confirmedPassword", "Confirmed Password Must Match");
        }
        User user = new User();
        user.setEmail(info.getEmail());
        user.setPasswordHash(passwordEncoder.encode(info.getPassword()));
        user.setFullName(info.getFullName());
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UniqueColumnViolationException(UniqueColumnViolationException.EMAIL_ALREADY_EXISTS);
        }
        AuthenticationResponseDTO authenticationResponseDTO = new AuthenticationResponseDTO();
        UserPrincipal userPrincipal = new UserPrincipal(user);
        Session session = sessionService.createSession(user);
        String accessToken = tokenService.generateAccessToken(userPrincipal, session.getSessionId());
        String refreshToken = tokenService.generateRefreshToken(userPrincipal, session.getSessionId());
        authenticationResponseDTO.setAccessToken(accessToken);
        authenticationResponseDTO.setRefreshToken(refreshToken);
        authenticationResponseDTO.setSession(session);
        return authenticationResponseDTO;
    }

    public AuthenticationResponseDTO login(AuthDTO credentials) throws BadCredentialsException {
        String password = credentials.getPassword();
        String identifier = credentials.getIdentifier();
        if (password == null || identifier == null) {
            throw new BadCredentialsException("Invalid Credentials");
        }
        UserPrincipal userDetails = userPrincipalService.loadUserByIdentifier(identifier);
        if (userDetails != null && passwordEncoder.matches(password, userDetails.getPasswordHash())) {
            User user = userDetails.getUser();
            AuthenticationResponseDTO authenticationResponseDTO = new AuthenticationResponseDTO();
            Session session = sessionService.createSession(user);
            String accessToken = tokenService.generateAccessToken(userDetails, session.getSessionId());
            String refreshToken = tokenService.generateRefreshToken(userDetails, session.getSessionId());
            authenticationResponseDTO.setAccessToken(accessToken);
            authenticationResponseDTO.setRefreshToken(refreshToken);
            sessionService.addSession(user.getUserId(), session);
            authenticationResponseDTO.setSession(session);
            return authenticationResponseDTO;
        }
        throw new BadCredentialsException(BadCredentialsException.INVALID_CREDENTIALS);
    }

    public AuthenticationResponseDTO oauth2Login(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", GRANT_TYPE);
        body.add("redirect_uri", REDIRECT_URI);
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        GoogleResponse response = restTemplate.postForObject(TOKEN_URL, request, GoogleResponse.class);
        HttpHeaders userInfoHeaders = new HttpHeaders();
        AuthenticationResponseDTO authenticationResponseDTO = new AuthenticationResponseDTO();
        if (response != null) {
            userInfoHeaders.setBearerAuth(response.getAccess_token());
            System.out.println(response.getAccess_token());
            HttpEntity<Void> requestEntity = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<GoogleResponse> userInfoResponse = restTemplate.exchange(USERINFO_ENDPOINT, HttpMethod.GET, requestEntity, GoogleResponse.class);
            GoogleResponse googleResponse = userInfoResponse.getBody();
            if (userInfoResponse.getStatusCode() == HttpStatus.OK && googleResponse != null) {
                UserPrincipal userPrincipal = userPrincipalService.loadUserByIdentifier(googleResponse.getEmail());
                if (userPrincipal == null) {
                    User user = new User();
                    user.setOauth2Id(googleResponse.getId());
                    user.setEmail(googleResponse.getEmail());
                    user.setProfilePicture(googleResponse.getPicture());
                    user.setFullName(googleResponse.getName());
                    try {
                        userRepository.save(user);
                    } catch (Exception e) {
                        throw new DataIntegrityViolationException(e.getMessage());
                    }
                    Session session = sessionService.createSession(user);
                    sessionService.addSession(user.getUserId(), session);
                    userPrincipal = new UserPrincipal(user);
                    String accessToken = tokenService.generateAccessToken(userPrincipal, session.getSessionId());
                    String refreshToken = tokenService.generateRefreshToken(userPrincipal, session.getSessionId());
                    authenticationResponseDTO.setAccessToken(accessToken);
                    authenticationResponseDTO.setRefreshToken(refreshToken);
                    authenticationResponseDTO.setSession(session);
                    sessionService.addSession(user.getUserId(), session);
                    authenticationResponseDTO.setSession(session);
                    return authenticationResponseDTO;
                } else if (userPrincipal.getUser().getOauth2Id() == null || userPrincipal.getUser().getOauth2Id().isEmpty()) {
                    throw new UniqueColumnViolationException(UniqueColumnViolationException.EMAIL_ALREADY_EXISTS);
                } else {
                    User user = userPrincipal.getUser();
                    Session session = sessionService.createSession(user);
                    String accessToken = tokenService.generateAccessToken(userPrincipal, session.getSessionId());
                    String refreshToken = tokenService.generateRefreshToken(userPrincipal, session.getSessionId());
                    sessionService.addSession(user.getUserId(), session);
                    authenticationResponseDTO.setSession(session);
                    authenticationResponseDTO.setAccessToken(accessToken);
                    authenticationResponseDTO.setRefreshToken(refreshToken);
                    return authenticationResponseDTO;
                }
            } else {
                throw new RuntimeException();
            }
        }
        throw new RuntimeException();
    }

    public Map<String, String> logout(UserPrincipal userDetails, Session session) {
        sessionService.deleteSession(userDetails.getUserId(), session);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logout Successful");
        return response;
    }

    public UserDTO getUserProfile(UserPrincipal userDetails) {
        User user = userDetails.getUser();
        return UserDTO.convertFromEntity(user);
    }

    public Set<Session> getSessions(UserPrincipal userDetails) {
        Set<Session> sessions = sessionService.getSessions(userDetails.getUserId());
        for (Session session : sessions) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");
            String sessionDescription = String.format("Created at %s on %s, IP address %s, Browser  %s", session.getCreatedAt().format(formatter), session.getBrowserName(), session.getIpAddress(), session.getUserAgent());
            session.setSessionDescription(sessionDescription);
        }
        Set<Session> sortedSessions = new TreeSet<>(sessions).descendingSet();
        return sortedSessions;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDTO updateUserProfile(UserPrincipal userDetails, UserDTO userDTO) {
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
    public Map<String, Object> changePassword(UserPrincipal userDetails, AuthDTO credentials) throws PassWordNotMatch {
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

    public List<UserAddressDTO> findUserAddresses(UserPrincipal userDetails) {
        List<UserAddress> userAddresses = userAddressRepository.findUserAddressesByUserId(userDetails.getUser().getUserId());
        List<UserAddressDTO> userAddressDTOs = new ArrayList<>();
        for (UserAddress userAddress : userAddresses) {
            UserAddressDTO userAddressDTO = UserAddressDTO.convertFromEntity(userAddress);
            userAddressDTOs.add(userAddressDTO);
        }
        return userAddressDTOs;
    }

    @Transactional
    public UserAddressDTO createAddress(UserPrincipal userDetails, UserAddressDTO userAddressDTO) {
        UserAddress userAddress = new UserAddress();
        userAddress.getCity().setCityName(userAddressDTO.getCity().getName());
        userAddress.getCity().setCityId(userAddressDTO.getCity().getId());
        userAddress.setFullName(userAddressDTO.getFullName());
        userAddress.setPhone(userAddressDTO.getPhone());
        userAddress.setDetailedAddress(userAddressDTO.getDetailedAddress());
        userAddress.setUser(userDetails.getUser());
        if (userAddressDTO.getId() != null) {
            userAddress.setUserAddressId(userAddressDTO.getId());
        }
        userAddressRepository.save(userAddress);
        userAddressDTO.setId(userAddress.getUserAddressId());
        userAddressDTO.setFullAddress(userAddress.getDetailedAddress()
                + ", " + userAddressDTO.getCity().getName()
                + ", " + userAddressDTO.getState().getName()
                + ", " + userAddressDTO.getCountry().getName());
        return userAddressDTO;
    }
}
