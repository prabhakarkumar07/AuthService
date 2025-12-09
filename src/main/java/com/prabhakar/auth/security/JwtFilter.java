package com.prabhakar.auth.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.prabhakar.auth.dto.ApiResponse;
import com.prabhakar.auth.service.BlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final BlacklistService blacklistService;

    public JwtFilter(JwtUtil jwtUtil,
                     UserDetailsService uds,
                     BlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = uds;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Allow only true public endpoints
        if (path.equals("/auth/login") ||
            path.equals("/auth/register") ||
            path.equals("/auth/refresh")) {
            chain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.extractTokenFromRequest(request);

        // No token → continue -> SecurityConfig will block protected endpoints
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        // 1) Token blacklisted → reject immediately
        if (blacklistService.isBlacklisted(token)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_BLACKLISTED",
                    "Token has been logged out");
            return;
        }


        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            // Invalid structure or signature → 401
        	writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
        	        "TOKEN_EXPIRED",
        	        "JWT token expired");

            return;
        }

        // 2) If token expired → reject immediately
        if (!jwtUtil.validateToken(token)) {
        	writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
        	        "TOKEN_EXPIRED",
        	        "JWT token expired");
        }

        // 3) Initialize SecurityContext if not already set
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails details = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            details,
                            null,
                            details.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
    
    
    private void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        ApiResponse<?> body = ApiResponse.error(status, error, message);

        // Convert to JSON (no adding dependencies)
        String json = """
            {
                "success": %s,
                "status": %d,
                "error": "%s",
                "message": "%s",
                "data": null
            }
            """.formatted(
                    body.isSuccess(),
                    body.getStatus(),
                    body.getError(),
                    body.getMessage()
            );

        response.getWriter().write(json);
    }

}
