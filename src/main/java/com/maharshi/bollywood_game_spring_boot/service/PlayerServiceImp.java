package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.dto.PlayerDto;
import com.maharshi.bollywood_game_spring_boot.dto.PlayerStatusDto;
import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.dto.SocketResponse;
import com.maharshi.bollywood_game_spring_boot.model.FriendVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import com.maharshi.bollywood_game_spring_boot.repository.FriendRepository;
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

    public void updatePlayerStatus(String playerName, String status,String gameId) {
        if (status.equals("Online")) {
            if (redisService.get(playerName, String.class) == null) {

                redisService.set(playerName, status, null);
                SocketResponse response = new SocketResponse(status, playerName, "status");
                messagingTemplate.convertAndSend("/topic/general", response);

            }
        } else if (status.equals("Offline")) {

            redisService.delete(playerName);
            SocketResponse response = new SocketResponse(status, playerName, "status");
            messagingTemplate.convertAndSend("/topic/general", response);

        } else if(status.equals("Hosting")||status.equals("In Game")) {

            redisService.set(playerName, status, null);
            SocketResponse response = new SocketResponse(status, playerName, "status");
            messagingTemplate.convertAndSend("/topic/general", response);
            PlayerStatusDto playerStatusDto=new PlayerStatusDto(playerName,status,gameId);
            SocketResponse response1 = new SocketResponse(status, playerStatusDto
                    , "playerInGameStatus");
            messagingTemplate.convertAndSend("/topic/game/"+gameId
                    , response1);
        }
    }

    @Override
    public String getPlayerStatus(String playerName) {
        String status = redisService.get(playerName, String.class);
        return status == null ? "Offline" : status;
    }

}
