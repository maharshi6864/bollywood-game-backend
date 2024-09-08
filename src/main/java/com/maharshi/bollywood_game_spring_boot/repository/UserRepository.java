package com.maharshi.bollywood_game_spring_boot.repository;


import com.maharshi.bollywood_game_spring_boot.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Integer> {
  List<User> findByUsername(String username);
}
