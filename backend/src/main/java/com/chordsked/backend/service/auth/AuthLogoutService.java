package com.chordsked.backend.service.auth;

public interface AuthLogoutService {
    void logout(String accessToken, String refreshToken);
}
