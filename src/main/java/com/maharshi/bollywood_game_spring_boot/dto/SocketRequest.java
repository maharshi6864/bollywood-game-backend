package com.maharshi.bollywood_game_spring_boot.dto;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketRequest<T> {

    private String message;

    private T body;

    private String type;

}
