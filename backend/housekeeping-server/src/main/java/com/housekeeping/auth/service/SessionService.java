package com.housekeeping.auth.service;

import com.housekeeping.auth.support.SessionUser;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private static final long EXPIRE_MILLIS = 12L * 60 * 60 * 1000;
    private final ConcurrentHashMap<String, SessionUser> sessions = new ConcurrentHashMap<>();

    public String createToken(Long userId, String phone, String realName, String roleCode) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new SessionUser(
                userId,
                phone,
                realName,
                roleCode,
                System.currentTimeMillis() + EXPIRE_MILLIS
        ));
        return token;
    }

    public SessionUser getSession(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        SessionUser sessionUser = sessions.get(token);
        if (sessionUser == null) {
            return null;
        }
        if (sessionUser.expireAt() < System.currentTimeMillis()) {
            sessions.remove(token);
            return null;
        }
        return sessionUser;
    }
}
