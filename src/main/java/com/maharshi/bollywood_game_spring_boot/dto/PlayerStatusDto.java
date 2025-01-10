package com.maharshi.bollywood_game_spring_boot.dto;

import lombok.*;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatusDto {

    private String playerName;

    private String status;

    private String gameId;
}
