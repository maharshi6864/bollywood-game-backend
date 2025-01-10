package com.maharshi.bollywood_game_spring_boot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player_vo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerVo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "playerName")
    private String playerName;

    @Column(name = "matchesPlayed")
    private int matchesPlayed;

    @Column(name = "points")
    private int points;

    @JoinColumn(name = "gameVo")
    @ManyToOne
    private GameVo gameVo;

}
