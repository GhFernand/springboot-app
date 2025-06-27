package com.semiddleware.integration.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${api.auth.key}")
    private String apiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	String key = request.getHeader("x-api-key");
        
        //#########PARA DEBBUGUER
        //System.out.println("🔑 Header recebido: [" + key + "]");
        //System.out.println("🔐 Chave esperada:  [" + apiKey + "]");
        //System.out.println("✅ São iguais? " + (key != null && key.trim().equals(apiKey)));

        if (key == null || !key.trim().equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("Acesso não autorizado: chave inválida.");
            return false;
        }

        return true;
    }
}
