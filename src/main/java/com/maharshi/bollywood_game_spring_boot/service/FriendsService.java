package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.dto.PlayerDto;
import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.dto.SocketRequest;
import com.maharshi.bollywood_game_spring_boot.dto.SocketResponse;

public interface FriendsService {

    Response findFriendsForPlayer();

    Response saveFriend(PlayerDto playerDto);

    Response removefriend(int id);
}
