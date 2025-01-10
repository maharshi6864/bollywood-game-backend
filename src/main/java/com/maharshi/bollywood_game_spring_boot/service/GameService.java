package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.dto.*;

public interface GameService {

    Response startGame(GameDto gameDto);

    Response getGameDetails();

    Response quitGame(GameDto gameDto);

    Response endGame(GameDto gameDto);

    Response gameRequestReply(PlayerStatusDto playerStatusDto);

    Response reSendRequest(PlayerStatusDto playerStatusDto);

    void askingGuessingStatus(SocketRequest socketRequest);

    void changePlayerInGameStatus(PlayerStatusDto playerStatusDto);

    void movieNameAsked(AskingAndGuessingDto askingAndGuessingDto);

    void guessMovieName(AskingAndGuessingDto askingAndGuessingDto);

    void completeThisRound(SocketRequest socketRequest);

    Response getHintForMovie(GameDto gameDto);

    Response getHintForMovieFromChatGpt(GameDto gameDto);

    void passHint(SocketRequest socketRequest);
}
