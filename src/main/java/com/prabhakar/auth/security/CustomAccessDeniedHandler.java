package com.prabhakar.auth.security;


import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prabhakar.auth.dto.ApiResponse;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

//        Map<String, Object> body = Map.of(
//                "status", 403,
//                "error", "ACCESS_DENIED",
//                "message", "You do not have permission to access this resource"
//        );
       
        new ObjectMapper().writeValue(response.getOutputStream(),  ApiResponse.error(403, "ACCESS_DENIED", "You do not have permission to access this resource")
);
    }
}
