package com.project.bookseller.service;

import com.project.bookseller.entity.user.Session;
import com.project.bookseller.entity.user.SessionStatus;
import com.project.bookseller.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


///!!!!!!!!REMEMBER: SPRING USES PROXY-BASED INTERCEPTION, MEANING THAT SINGLE-CLASS METHOD INVOCATIONS WILL NOT BE INTERCEPTED TWICE
@Service
@RequiredArgsConstructor
public class SessionService {
    private final GetSessionService getSessionsService;
    private final DeleteSessionService deleteSessionService;
    private final CreateSessionService createSessionService;
    private final UpdateSessionService updateSessionService;

    public static String generateSessionId(Long userId) {
        return userId + "_" + Instant.now().toEpochMilli() + "_" + UUID.randomUUID();
    }

    public Session createSession(User user) {
        String sessionId = SessionService.generateSessionId(user.getUserId());
        Session session = new Session();
        session.setCreatedAt(LocalDateTime.now());
        session.setSessionId(sessionId);
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setExpiredAt(session.getCreatedAt().plusHours(24));
        return session;
    }

    public Session getSession(Long userId, String sessionId) {
        Set<Session> sessions = getSessionsService.getSessions(userId);
        return sessions.stream().filter(s -> s.getSessionId().equals(sessionId)).findFirst().orElse(null);
    }

    public void deleteAllSession(Long userId) {
        deleteSessionService.deleteAllSessions(userId);
    }

    public Set<Session> getSessions(Long userId) {
        return getSessionsService.getSessions(userId);
    }

    public Set<Session> addSession(Long userId, Session session) {
        return createSessionService.addSession(userId, session);
    }

    public void deleteSession(Long userId, Session session) {
        deleteSessionService.deleteSession(userId, session);
    }

    public Set<Session> extendSession(Long userId, Session session, LocalDateTime newExpirationTime) {
        return updateSessionService.extendSession(userId, session, newExpirationTime);
    }
}

@Service
class GetSessionService {
    @Cacheable(value = "sessions", key = "#userId")
    public Set<Session> getSessions(Long userId) {
        return new HashSet<>();
    }
}

@Service
@RequiredArgsConstructor
class CreateSessionService {
    private final GetSessionService getSessionsService;

    @CachePut(value = "sessions", key = "#userId")
    public Set<Session> addSession(Long userId, Session session) {
        Set<Session> sessions = getSessionsService.getSessions(userId);
        sessions.add(session);
        return sessions;
    }
}

@Service
@RequiredArgsConstructor
class DeleteSessionService {
    private final GetSessionService getSessionsService;

    @CachePut(value = "sessions", key = "#userId")
    public Set<Session> deleteSession(Long userId, Session session) {
        Set<Session> sessions = getSessionsService.getSessions(userId);
        sessions.stream().filter(s -> s.equals(session)).forEach(s -> s.setSessionStatus(SessionStatus.INACTIVE));
        return sessions;
    }

    @CachePut(value = "sessions", key = "#userId")
    public Set<Session> deleteAllSessions(Long userId) {
        Set<Session> sessions = getSessionsService.getSessions(userId);
        for (Session session : sessions) {
            session.setSessionStatus(SessionStatus.INACTIVE);
        }
        return sessions;
    }
}

@Service
@RequiredArgsConstructor
class UpdateSessionService {
    private final GetSessionService getSessionsService;

    @CachePut(value = "sessions", key = "#userId")
    public Set<Session> extendSession(Long userId, Session session, LocalDateTime newExpirationTime) {
        Set<Session> sessions = getSessionsService.getSessions(userId);
        sessions.stream().filter(s -> s.equals(session)).forEach(s -> s.setExpiredAt(newExpirationTime));
        return sessions;
    }
}