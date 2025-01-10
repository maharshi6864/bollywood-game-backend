package com.maharshi.bollywood_game_spring_boot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AskingAndGuessingDto {

    private int id;

    private String askerPlayerName;

    private String guesserPlayerName;

    private String askedMovieName;

    private String guessedWordOrLetter;

    private String timeStamp;

}
