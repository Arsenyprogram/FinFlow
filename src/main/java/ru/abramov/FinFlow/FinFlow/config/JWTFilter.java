package ru.abramov.FinFlow.FinFlow.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.abramov.FinFlow.FinFlow.exception.ExpiredTokenException;
import ru.abramov.FinFlow.FinFlow.security.JWTUtil;

import ru.abramov.FinFlow.FinFlow.service.PersonDetailsService;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final PersonDetailsService personDetailsService;

    public JWTFilter(JWTUtil jwtUtil, PersonDetailsService personDetailsService) {
        this.jwtUtil = jwtUtil;
        this.personDetailsService = personDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorizationHeader = request.getHeader("Authorization");


        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if(token.isBlank()){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
                return;
            }else {
                try {
                    String userName = jwtUtil.verifyAccessToken(token);
                    UserDetails userDetails = personDetailsService.loadUserByUsername(userName);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                    if(SecurityContextHolder.getContext().getAuthentication() == null){
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (JWTVerificationException e) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Access Токен уже не действует");
                    return;
                }
            }
        }
        else {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Access Токен уже не действует");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8"); // <- важно!
        response.setCharacterEncoding("UTF-8");                   // <- обязательно!
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

}
