package com.capstone.personalityTest.filter;

import com.capstone.personalityTest.service.UserInfoDetails;
import com.capstone.personalityTest.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; //extends OncePerRequestFilter → ensures that this filter is executed once per HTTP request, Purpose: Intercept every request and check if it contains a valid JWT.

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {


    private final UserDetailsService userDetailsService; //UserDetailsService → loads user info (username, roles, password) from your database.
    private final JwtService jwtService; //JwtService → custom service that creates, extracts, and validates JWTs.

    @Autowired
    public JwtAuthFilter(UserDetailsService userDetailsService ,  JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        // Skip JWT filter for public endpoints
        if (path.startsWith("/auth/signUp") || path.startsWith("/auth/logIn") || path.startsWith("/auth/welcome")) {
            filterChain.doFilter(request, response);
            return;
        }

        //Extract Authorization Header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;


        // Check and Extract JWT
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // remove "Bearer " prefix
            username = jwtService.extractUsername(token); //to get the username stored inside the JWT, JWT contains the username and roles; extracting them allows Spring Security to know who the user is.
        }

//        Checks if the user is not already authenticated in Spring Security (SecurityContextHolder).
//         Prevents re-authenticating the same request multiple times.If for some reason the filter code runs again in the same request, the check prevents doing the work twice.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //Spring Security only requires the interface methods (getUsername(), getPassword(), getAuthorities()), so the filter works with any implementation.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.validateToken(token)) {
                //UsernamePasswordAuthenticationToken (Spring Security object)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, //username since we trust jwt
                        userDetails.getAuthorities()); //Sets the roles/authorities for access control
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //Adds details about the request (IP, session, etc.) for auditing.

                SecurityContextHolder.getContext().setAuthentication(authToken); //Stores it in SecurityContextHolder → now Spring Security knows the user is authenticated for this request.
            }
        }

        filterChain.doFilter(request, response); //Calls the next filter or the controller.If JWT was valid → user is authenticated → controller can enforce roles/access.If JWT was invalid or missing → controller will reject the request (Spring Security handles this).




    }
}
