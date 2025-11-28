package com.prabhakar.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prabhakar.auth.dto.ApiResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

//        Map<String, Object> body = Map.of(
//                "status", 401,
//                "error", "UNAUTHORIZED",
//                "message", "Invalid or missing authentication token"
//        );
        

        new ObjectMapper().writeValue(response.getOutputStream(), ApiResponse.error(401, "UNAUTHORIZED", "Invalid or missing authentication token"));
    }
}
