package com.maharshi.bollywood_game_spring_boot.repository;

import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRepository extends CrudRepository<PlayerVo, Integer> {

    // Custom query to search by player name or associated username
    @Query("SELECT p FROM PlayerVo p LEFT JOIN UserVo u ON u.playerVo = p " +
            "WHERE LOWER(p.playerName) LIKE LOWER(CONCAT('%', :searchString, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchString, '%'))")
    List<PlayerVo> searchThroughString(@Param("searchString") String searchString);

    PlayerVo findByPlayerName(String playerName);

    PlayerVo findById(int id);
}
