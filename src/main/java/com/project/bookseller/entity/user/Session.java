package com.project.bookseller.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class Session implements Serializable, Comparable<Session> {
    @Serial
    private static final long serialVersionUID = 10L;
    private String sessionId;
    private String deviceType;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private SessionStatus sessionStatus;
    private String ipAddress;
    private String browserName;
    private String osName;
    private String osVersion;
    private String timestamp;
    private String userAgent;
    private String sessionDescription;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(getSessionId(), session.getSessionId());
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(getSessionId());
    }
    @Override
    public int compareTo(Session other) {
        if (this.createdAt == null && other.createdAt == null) {
            return 0;
        } else if (this.createdAt == null) {
            return -1;
        } else if (other.createdAt == null) {
            return 1;
        }
        return this.createdAt.compareTo(other.createdAt);
    }
}
