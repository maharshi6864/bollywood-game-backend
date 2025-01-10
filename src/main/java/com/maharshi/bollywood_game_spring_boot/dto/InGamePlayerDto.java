package com.maharshi.bollywood_game_spring_boot.dto;

import lombok.*;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InGamePlayerDto {

    private int id;

    private int gamePoints;

    private String playerName;

    private int gameId;

    private int sequenceNumber;

    private String status;
}
