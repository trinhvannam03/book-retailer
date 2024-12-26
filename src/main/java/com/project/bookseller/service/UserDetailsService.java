package com.project.bookseller.service;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.entity.user.User;
import com.project.bookseller.repository.UserRepository;
import com.project.bookseller.validation.IdentifierTypeValidator;
import com.project.bookseller.validation.IdentifierType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsService {
    private final UserRepository userRepository;

    public UserDetails loadUserDetailsByIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }
        IdentifierType identifierType = IdentifierTypeValidator.resolveIdentifier(identifier);
        Optional<User> optionalUser;
        if (identifierType == IdentifierType.UNKNOWN) {
            return null;
        } else if (identifierType == IdentifierType.PHONE) {
            optionalUser = userRepository.findUserByPhone(identifier);
        } else {
            optionalUser = userRepository.findUserByEmail(identifier);
        }
        return optionalUser.map(UserDetails::new).orElse(null);
    }
}
