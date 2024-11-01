package com.maharshi.bollywood_game_spring_boot.dto;

import jakarta.persistence.Column;
import lombok.*;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {
    private int id;

    private String playerName;

    private int matchesPlayed;

    private int matchesWon;

    private boolean friend;

}
