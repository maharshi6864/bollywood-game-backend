package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.model.User;

public interface UserService {

  void insertUser(User user);

  User findByUserName(String username);

  User getCurrentUser();
}
