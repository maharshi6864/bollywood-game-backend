package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;

import java.util.List;

public interface PlayerService {

    PlayerVo savePLayer(PlayerVo playerVo);

    Response searchThroughString(String searchString);
}
