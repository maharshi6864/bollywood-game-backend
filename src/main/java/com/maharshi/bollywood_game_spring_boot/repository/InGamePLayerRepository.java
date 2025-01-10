package com.maharshi.bollywood_game_spring_boot.repository;


import com.maharshi.bollywood_game_spring_boot.model.FriendVo;
import com.maharshi.bollywood_game_spring_boot.model.GameVo;
import com.maharshi.bollywood_game_spring_boot.model.InGamePlayerVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InGamePLayerRepository extends CrudRepository<InGamePlayerVo, Integer> {

    List<InGamePlayerVo> findByGameVo(GameVo gameVo);

    InGamePlayerVo findById(int id);

    List<InGamePlayerVo> findByJoinedStatus(String joinedStatus);

}
