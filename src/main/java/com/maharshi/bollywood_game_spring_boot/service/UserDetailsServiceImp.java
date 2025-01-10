package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserDetailsServiceImp implements AuthenticationProvider, UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserVo userVo = this.userService.findByUserName(username);
        if (userVo != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(userVo.getUsername())
                    .password(userVo.getPassword())
                    .roles(userVo.getRole())
                    .build();

        }
        throw new UsernameNotFoundException("Username not found !!!" + username);
    }

    private Authentication createSuccessfulAuthentication(final Authentication authentication, final UserDetails user) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(),
                authentication.getPrincipal(), user.getAuthorities());
        token.setDetails(authentication.getDetails());
        return token;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        UserDetails user = null;
        try {
            user = loadUserByUsername(username);

        } catch (UsernameNotFoundException exception) {
            throw new BadCredentialsException("invalid login details");
//            log.info("Invalid Credentials");
        }

        // Get the raw password entered by the user
        String rawPassword = authentication.getCredentials().toString();

        // Compare the raw password with the stored encoded password
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid login details"); // If the passwords do not match, throw an exception
//            log.info("Invalid Credentials");
        }
        return createSuccessfulAuthentication(authentication, user);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
