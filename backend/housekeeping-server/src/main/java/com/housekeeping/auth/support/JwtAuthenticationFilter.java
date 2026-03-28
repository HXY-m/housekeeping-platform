package com.housekeeping.auth.support;

import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.service.AuthAccountService;
import com.housekeeping.auth.service.JwtTokenService;
import com.housekeeping.auth.service.RolePermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AuthAccountService authAccountService;
    private final RolePermissionService rolePermissionService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AuthAccountService authAccountService,
                                   RolePermissionService rolePermissionService) {
        this.jwtTokenService = jwtTokenService;
        this.authAccountService = authAccountService;
        this.rolePermissionService = rolePermissionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token != null && !token.isBlank()) {
                SessionUser tokenUser = jwtTokenService.parseToken(token);
                SysUserEntity user = authAccountService.requireActiveUserById(tokenUser.userId());
                if (!authAccountService.userHasRole(user.getId(), tokenUser.roleCode())) {
                    throw new IllegalArgumentException("role not matched");
                }

                List<String> permissionCodes = rolePermissionService.listPermissionCodes(user.getId(), tokenUser.roleCode());
                SessionUser authenticatedUser = new SessionUser(
                        user.getId(),
                        user.getPhone(),
                        user.getRealName(),
                        tokenUser.roleCode(),
                        permissionCodes,
                        tokenUser.expireAt()
                );

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.roleCode()));
                permissionCodes.forEach(code -> authorities.add(new SimpleGrantedAuthority(code)));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authenticatedUser, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                CurrentUserContext.set(authenticatedUser);
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
            CurrentUserContext.clear();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
            CurrentUserContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("X-Auth-Token");
    }
}
