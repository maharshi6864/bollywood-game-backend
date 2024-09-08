package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.model.User;
import com.maharshi.bollywood_game_spring_boot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void insertUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User findByUserName(String username) {
        return this.userRepository.findByUsername(username).get(0);
    }

    @Override
    public User getCurrentUser() {
        return this.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
    }

}
