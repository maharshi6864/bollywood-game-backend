package com.maharshi.bollywood_game_spring_boot.dto;

import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameDto {

    private int id;

    private int hostPlayerId;

    private String hostPlayerName;

    private List<FriendsDto> friendsDtoList;

    private List<InGamePlayerDto> inGamePlayerDtoList;

    private int currentPlayerChanceIndex;

    private String currentChancePlayerName;

    private String movieName;

    private String actualMovieName;

    private String uniqueAlphabets;

    private String roundTimeOut;

    private String hint;

    private String pauseSeconds;

    private int chanceLeft;

    private List<String> previousGuesses;

    private int totalPlayer;

    private int totalPlayerOnline;

}
