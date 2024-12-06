package com.maharshi.bollywood_game_spring_boot.repository;

import com.maharshi.bollywood_game_spring_boot.model.FriendVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FriendRepository extends CrudRepository<FriendVo, Integer> {

    List<FriendVo> findByPlayerVoAndFriendInfo(PlayerVo playerVo, PlayerVo friendInfo);

    List<FriendVo> findByPlayerVoOrFriendInfo(PlayerVo playerVo, PlayerVo friendInfo);

    List<FriendVo> findByPlayerVo(PlayerVo playerVo);
}
