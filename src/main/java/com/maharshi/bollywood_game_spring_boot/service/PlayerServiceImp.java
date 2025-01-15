package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.dto.*;
import com.maharshi.bollywood_game_spring_boot.model.FriendVo;
import com.maharshi.bollywood_game_spring_boot.model.GameVo;
import com.maharshi.bollywood_game_spring_boot.model.InGamePlayerVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import com.maharshi.bollywood_game_spring_boot.repository.FriendRepository;
import com.maharshi.bollywood_game_spring_boot.repository.GameRepository;
import com.maharshi.bollywood_game_spring_boot.repository.InGamePLayerRepository;
import com.maharshi.bollywood_game_spring_boot.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class PlayerServiceImp implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private InGamePLayerRepository inGamePLayerRepository;


    @Override
    public PlayerVo savePLayer(PlayerVo playerVo) {
        return playerRepository.save(playerVo);
    }

    @Override
    public Response searchThroughString(String searchString) {
        List<PlayerDto> playerDtoList = new ArrayList<>();
        List<PlayerVo> playerVoList = this.playerRepository.searchThroughString(searchString);

        PlayerVo currentPlayer = this.playerRepository
                .findByPlayerName(this.userService.getCurrentUser().getUsername());


        for (PlayerVo playerVo : playerVoList) {
            List<FriendVo> friendVoList = this.friendRepository
                    .findByPlayerVoAndFriendInfo(currentPlayer, playerVo);
            System.out.println(playerVo.getId());
            // Check if player is a friend
            boolean isFriend = !friendVoList.isEmpty();


            PlayerDto playerDto = new PlayerDto(
                    playerVo.getId(),
                    playerVo.getPlayerName(),
                    playerVo.getMatchesPlayed(),
                    playerVo.getPoints(),
                    isFriend
            );

            // Only add if the player name does not match the current user
            if (!playerVo.getPlayerName()
                    .equals(this.userService.getCurrentUser().getUsername())) {
                playerDto.setFriend(isFriend);  // Set the friend status
                playerDtoList.add(playerDto);
                System.out.println(friendVoList.size());
                System.out.println(isFriend); // Print to confirm
            }
        }

        return new Response("success", playerDtoList, true);
    }

    @Override
    public PlayerVo getCurrentPlayer() {
        return this.playerRepository
                .findByPlayerName(this.userService.getCurrentUser().getUsername());
    }

    public void updatePlayerStatus(String playerName, String status, String gameId) {
        switch (status) {
            case "Online" -> {
                if (redisService.get(playerName, PlayerStatusDto.class) == null) {
                PlayerStatusDto playerStatusDto = new PlayerStatusDto(playerName, status, gameId);
                redisService.set(playerName, playerStatusDto, null);
                SocketResponse response = new SocketResponse(status, playerName, "status");
                messagingTemplate.convertAndSend("/topic/general", response);
                }
            }
            case "Offline" -> {
                PlayerStatusDto playerStatusDto = redisService.get(playerName, PlayerStatusDto.class);

                if (playerStatusDto != null) {
                    if (playerStatusDto.getGameId() != null) {
                        GameDto gameDto = redisService.get(playerStatusDto.getGameId(), GameDto.class);
                        if (gameDto != null) {
                            for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
                                if (inGamePlayerDto.getPlayerName().equals(playerName)) {
                                    inGamePlayerDto.setStatus("Offline");
                                    break;
                                }
                            }
                            // Update the game in Redis
                            redisService.set(playerStatusDto.getGameId(), gameDto, null);

                            // Notify game-specific topic
                            SocketResponse gameResponse = new SocketResponse("Offline", playerStatusDto, "playerInGameStatus");
                            messagingTemplate.convertAndSend("/topic/game/" + playerStatusDto.getGameId(), gameResponse);

                            // Notify general topic
                            SocketResponse generalResponse = new SocketResponse("Offline", playerName, "status");
                            messagingTemplate.convertAndSend("/topic/general", generalResponse);

                            // Handle someone leaving the game
                            this.someOneLeftTheGame(gameDto);
                        }
                    } else {
                        // Player is not associated with any game, update and notify general topic
                        playerStatusDto.setStatus("Offline");
                        SocketResponse generalResponse = new SocketResponse("Offline", playerName, "status");
                        messagingTemplate.convertAndSend("/topic/general", generalResponse);
                    }
                }
            }
            case "Host", "In Game" -> {
                PlayerStatusDto playerStatusDto = new PlayerStatusDto(playerName, status, gameId);
                redisService.delete(playerName);
                redisService.set(playerName, playerStatusDto, null);
                SocketResponse response = new SocketResponse(status, playerName, "status");
                messagingTemplate.convertAndSend("/topic/general", response);
                SocketResponse response1 = new SocketResponse(status, playerStatusDto
                        , "playerInGameStatus");
                messagingTemplate.convertAndSend("/topic/game/" + gameId
                        , response1);
                this.someOneJoinedTheGame((GameDto) redisService.get(gameId, GameDto.class));
            }
            case "Denied" -> {
                PlayerStatusDto playerStatusDto = new PlayerStatusDto(playerName, status, gameId);
                redisService.set(playerName, playerStatusDto, null);
                SocketResponse response1 = new SocketResponse(status, playerStatusDto
                        , "playerInGameStatus");
                messagingTemplate.convertAndSend("/topic/game/" + gameId
                        , response1);
            }
        }
    }


    @Override
    public void someOneLeftTheGame(GameDto gameDto) {
        long playersOnline = gameDto.getInGamePlayerDtoList().stream()
                .filter(inGamePlayerDto ->
                        "Host".equals(inGamePlayerDto.getStatus()) || "In Game".equals(inGamePlayerDto.getStatus()))
                .count();
        if (playersOnline == 1) {
            long roundComplitionTime = Long.parseLong(gameDto.getRoundTimeOut());
            gameDto.setPauseSeconds(String.valueOf(roundComplitionTime - System.currentTimeMillis()));
            redisService.set(String.valueOf(gameDto.getId()), gameDto, null);
            SocketResponse response1 = new SocketResponse("GamePaused"
                    , gameDto
                    , "updateGame");
            messagingTemplate
                    .convertAndSend("/topic/game/"
                            + gameDto.getId(), response1);
        }
        if (playersOnline == 0) {
            for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
                InGamePlayerVo inGamePlayerVo = this.inGamePLayerRepository.findById(inGamePlayerDto.getId());
                inGamePlayerVo.setSequenceNumber(inGamePlayerDto.getSequenceNumber());
                inGamePlayerVo.setGamePoints(inGamePlayerDto.getGamePoints());
                this.inGamePLayerRepository.save(inGamePlayerVo);
            }

            GameVo gameVo = this.gameRepository.findById(gameDto.getId());
            gameVo.setChanceLeft(gameDto.getChanceLeft());
            gameVo.setPreviousGuesses(gameDto.getPreviousGuesses());
            gameVo.setCurrentPlayerChanceIndex(gameDto.getCurrentPlayerChanceIndex());
            gameVo.setMovieName(gameDto.getMovieName());
            gameVo.setRoundTimeOut(gameDto.getRoundTimeOut());
            gameVo.setUniqueAlphabets(gameDto.getUniqueAlphabets());
            gameVo.setTotalPlayer(gameDto.getTotalPlayer());
            gameVo.setCurrentChancePlayerName(gameDto.getCurrentChancePlayerName());
            gameVo.setActualMovieName(gameDto.getActualMovieName());
            gameVo.setPauseSeconds(gameDto.getPauseSeconds());
            this.gameRepository.save(gameVo);
        }
    }

    @Override
    public void someOneJoinedTheGame(GameDto gameDto) {

    }

    @Override
    public String getPlayerStatus(String playerName) {
        String status = redisService.get(playerName, String.class);
        return status == null ? "Offline" : status;
    }

}
