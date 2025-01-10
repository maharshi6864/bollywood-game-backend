package com.maharshi.bollywood_game_spring_boot.repository;

import com.maharshi.bollywood_game_spring_boot.model.FriendVo;
import com.maharshi.bollywood_game_spring_boot.model.GameVo;
import com.maharshi.bollywood_game_spring_boot.model.InGamePlayerVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameRepository extends CrudRepository<GameVo, Integer> {

    GameVo findById(int id);

}
