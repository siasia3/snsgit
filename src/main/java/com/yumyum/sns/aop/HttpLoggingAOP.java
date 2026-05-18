package com.yumyum.sns.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class HttpLoggingAOP {

    // WebSocket 컨트롤러는 HTTP 요청이 없으므로 제외
    @Around("execution(* com.yumyum.sns.*.controller..*(..)) " +
            "&& !within(com.yumyum.sns.chat.controller.ChatSocketController)")
    public Object logHttpRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String handler = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "#" + joinPoint.getSignature().getName();

        log.info("[HTTP] {} {} | handler={} | ip={}", method, uri, handler, ip);

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        if (result instanceof ResponseEntity<?> response) {
            log.info("[HTTP] {} {} | status={} | {}ms", method, uri, response.getStatusCode().value(), duration);
        } else {
            log.info("[HTTP] {} {} | completed | {}ms", method, uri, duration);
        }

        return result;
    }
}