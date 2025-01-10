package com.maharshi.bollywood_game_spring_boot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "game_vo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameVo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @JoinColumn(name = "playerVo")
    @ManyToOne
    private PlayerVo hostPlayerVo;

    @Column(name = "roundTimeOut")
    private String roundTimeOut;

    @Column(name = "pauseSeconds")
    private String pauseSeconds;

    @Column(name = "movieName")
    private String movieName;

    @Column(name = "actualMovieName")
    private String actualMovieName;

    @Column(name = "uniqueAlphabets")
    private String uniqueAlphabets;

    @Column(name = "hint")
    private String hint;

    @Column(name = "chanceLeft")
    private int chanceLeft;

    @Column(name = "previousGuesses")
    private List<String> previousGuesses;

    @Column(name = "currentPlayerChanceIndex")
    private int currentPlayerChanceIndex;

    @Column(name = "totalPlayer")
    private int totalPlayer;

    @Column(name = "currentChancePlayerName")
    private String currentChancePlayerName;

}
