package com.maharshi.bollywood_game_spring_boot.model;

import jakarta.persistence.*;

import java.time.Instant;


@Entity
@Table(name = "opt_vo")
public class OTPVo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "otp")
    private String otp;

    @JoinColumn(name = "userId")
    @ManyToOne
    private User user;

    private Instant expireDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Instant expireDate) {
        this.expireDate = expireDate;
    }
}
