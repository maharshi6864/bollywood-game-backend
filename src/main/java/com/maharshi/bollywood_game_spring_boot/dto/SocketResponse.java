package com.maharshi.bollywood_game_spring_boot.dto;


import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketResponse {

    private String message;

    private Object body;

    private String type;
}
