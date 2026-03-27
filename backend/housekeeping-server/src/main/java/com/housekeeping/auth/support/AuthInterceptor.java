package com.housekeeping.auth.support;

import com.housekeeping.auth.annotation.RequireLogin;
import com.housekeeping.auth.annotation.RequireRole;
import com.housekeeping.auth.service.SessionService;
import com.housekeeping.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    public AuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireLogin requireLogin = findRequireLogin(handlerMethod);
        RequireRole requireRole = findRequireRole(handlerMethod);
        if (requireLogin == null && requireRole == null) {
            return true;
        }

        String token = resolveToken(request);
        SessionUser sessionUser = sessionService.getSession(token);
        if (sessionUser == null) {
            throw new BusinessException("登录状态已失效或未登录");
        }

        CurrentUserContext.set(sessionUser);
        if (requireRole != null && Arrays.stream(requireRole.value()).noneMatch(role -> role.equalsIgnoreCase(sessionUser.roleCode()))) {
            throw new BusinessException("无权限访问该接口");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("X-Auth-Token");
    }

    private RequireLogin findRequireLogin(HandlerMethod handlerMethod) {
        RequireLogin method = handlerMethod.getMethodAnnotation(RequireLogin.class);
        return method != null ? method : handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
    }

    private RequireRole findRequireRole(HandlerMethod handlerMethod) {
        RequireRole method = handlerMethod.getMethodAnnotation(RequireRole.class);
        return method != null ? method : handlerMethod.getBeanType().getAnnotation(RequireRole.class);
    }
}
