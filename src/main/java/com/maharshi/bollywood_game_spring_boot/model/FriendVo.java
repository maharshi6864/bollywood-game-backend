package com.maharshi.bollywood_game_spring_boot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "friend_vo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendVo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @JoinColumn(name = "player_vo")
    @ManyToOne
    private PlayerVo playerVo;

    @JoinColumn(name = "friend_info")
    @ManyToOne
    private PlayerVo friendInfo;

    @Column(name = "matches_played_together")
    private int matchesPlayedTogether;

}
