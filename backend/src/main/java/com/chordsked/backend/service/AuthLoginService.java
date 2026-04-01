package com.chordsked.backend.service;

import com.chordsked.backend.model.dto.AuthLoginRequest;
import com.chordsked.backend.model.vo.AuthLoginResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthLoginService {
    AuthLoginResultVO login(AuthLoginRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
