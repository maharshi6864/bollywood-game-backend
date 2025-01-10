package com.maharshi.bollywood_game_spring_boot.repository;


import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<UserVo, Integer> {
  List<UserVo> findByUsername(String username);
}
