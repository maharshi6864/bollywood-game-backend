package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.model.User;
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

@Component
public class UserDetailsServiceImp implements AuthenticationProvider, UserDetailsService {

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = this.userService.findByUserName(username);
    if (user != null) {
      return org.springframework.security.core.userdetails.User.builder()
          .username(user.getUsername())
          .password(user.getPassword())
          .roles(user.getRole())
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
    }

    // Get the raw password entered by the user
    String rawPassword = authentication.getCredentials().toString();

    // Compare the raw password with the stored encoded password
    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new BadCredentialsException("Invalid login details"); // If the passwords do not match, throw an exception
    }
    return createSuccessfulAuthentication(authentication, user);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

}
