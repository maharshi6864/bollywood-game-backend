package com.maharshi.bollywood_game_spring_boot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "in_game_player_vo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InGamePlayerVo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "gamePoints")
    private int gamePoints;

    @JoinColumn(name = "playerVo")
    @ManyToOne
    private PlayerVo playerVo;

    @JoinColumn(name = "gameVo")
    @ManyToOne
    private GameVo gameVo;

    @Column(name = "sequenceNumber")
    private int sequenceNumber;

    @Column(name="joinedStatus")
    private String joinedStatus;

}
