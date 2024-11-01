package com.maharshi.bollywood_game_spring_boot.dto;

import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendsDto {

    private int id;

    private String friendName;

    private int matchesWonTogether;

    private int matchesPlayedTogether;

    private int matchesPlayedByFriend;

    private int matchesWonByFriend;

    private boolean online  ;

}
