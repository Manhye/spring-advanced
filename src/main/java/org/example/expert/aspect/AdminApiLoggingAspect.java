package org.example.expert.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.config.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminApiLoggingAspect {

    private final JwtUtil jwtUtil;

    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        Long userId = jwtUtil.getUserId(token);
        String uri = request.getRequestURI();

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = extractRequestBody(joinPoint.getArgs(), mapper);
        log.info("Admin Request: UserId: {}, Time: {}, URI: {}, Request: {}",
                userId, LocalDateTime.now(), request.getRequestURI(), requestBody);

        Object result = null;
        try {
            result = joinPoint.proceed();

            String responseBody = mapper.writeValueAsString(result);
            log.info("Admin Response: UserId: {}, Time: {}, URI: {}, Response: {}",
                    userId, LocalDateTime.now(), uri, responseBody);
            return result;

        } catch (Throwable ex) {
            log.error("Admin Error: UserId: {}, Time: {}, URI: {}, Error: {}",
                    userId, LocalDateTime.now(), uri, ex.getMessage(), ex);
            throw ex;
        }
    }

    private String extractRequestBody(Object[] args, ObjectMapper mapper) {
        for (Object arg : args) {
            if (arg != null && !(arg instanceof HttpServletRequest)) {
                try {
                    return mapper.writeValueAsString(arg);
                } catch (JsonProcessingException e) {
                    return "Request body could not be converted to JSON";
                }
            }
        }
        return "No request body found";
    }

}
