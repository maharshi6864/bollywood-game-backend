package com.maharshi.bollywood_game_spring_boot.filter;


import com.maharshi.bollywood_game_spring_boot.service.UserDetailsServiceImp;
import com.maharshi.bollywood_game_spring_boot.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImp userDetailsServiceImp;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = this.getJwtFromCookies(request);
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {

            jwt = authorizationHeader.substring(6);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                ResponseCookie deleteCookie = ResponseCookie.from("Authentication", "")
                        .httpOnly(true)
                        .secure(true) // Use true if using HTTPS
                        .path("/") // Ensure this matches the path of the original cookie
                        .maxAge(0) // Expire the cookie immediately
                        .sameSite("Strict") // Match SameSite attribute
                        .build();
                // Add the delete cookie to the response
                response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
            }
        }
        if (username != null) {
            UserDetails userDetails = userDetailsServiceImp.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authentication".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
