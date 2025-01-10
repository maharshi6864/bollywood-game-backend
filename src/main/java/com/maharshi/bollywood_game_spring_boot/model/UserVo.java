package com.maharshi.bollywood_game_spring_boot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_vo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "username")
  private String username;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "role")
  private String role;

  @Column(name = "status")
  private boolean status;

  @JoinColumn(name = "playerVo")
  @ManyToOne
  private PlayerVo playerVo;

}
