package com.example.chat.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.chat.service.LogoutService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final LogoutService logoutService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain chain
    ) throws ServletException, IOException {
        
        String token = extractToken(request);

        if (token != null) {
            try {
                // 驗證 token 有效性
                if (logoutService.isTokenInvalid(token) || jwtUtil.isTokenExpired(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token is invalid or expired");
                    response.getWriter().flush();  // 確保訊息被發送
                    return;
                }

                // 取出使用者名稱並設置至 SecurityContext
                String username = jwtUtil.extractUsername(token);
                var auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("JWT verified, user: " + username);
            } catch (io.jsonwebtoken.ExpiredJwtException e){
                System.err.println("Token 已過期：" + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expired");
                response.getWriter().flush();
                return;
            } catch (io.jsonwebtoken.JwtException e) {
                System.err.println("Token 驗證失敗：" + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token invalid");
                response.getWriter().flush();
                return;
            } catch (Exception e) {
                System.err.println("Filter 發生未知錯誤：" + e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        // Authorization 標頭必須以 "Bearer " 開頭，後面才是 Token
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // 去掉 "Bearer " 前綴
        }
        return null;
    }
}

