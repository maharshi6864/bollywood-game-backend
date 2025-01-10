package com.maharshi.bollywood_game_spring_boot.dto;

import com.maharshi.bollywood_game_spring_boot.model.GameVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

//    private int id;

    private String username;

    private String email;

    private int playerId;

    private String playerName;

    private int matchesPlayed;

    private int points;

    private String inAGame;

}
