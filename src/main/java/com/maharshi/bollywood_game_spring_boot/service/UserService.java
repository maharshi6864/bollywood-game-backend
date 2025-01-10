package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;

public interface UserService {

  void insertUser(UserVo userVo);

  UserVo findByUserName(String username);

  UserVo getCurrentUser();

  Response getUserDetails();
}
