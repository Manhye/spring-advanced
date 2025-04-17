package org.example.expert.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tokenValue = request.getHeader("Authorization");

        if(!StringUtils.hasText(tokenValue)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No Authorization header found");
            return false;
        }

        try{
            String role = jwtUtil.getUserRole(tokenValue);
            Long userId = jwtUtil.getUserId(tokenValue);

            if(!"Admin".equals(role)){
                log.warn("Not allowed to access admin privileges: UserId: {}, Time: {}, URI: {}",
                        userId, LocalDateTime.now(), request.getRequestURI());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
                return false;
            }
            log.info("Admin logged in: UserId: {}, Time: {}, URI: {}",
                    userId, LocalDateTime.now(), request.getRequestURI());
            return true;
        }catch(Exception e){
            log.error("Token validation failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }


    }

}
