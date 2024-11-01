package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.dto.*;
import com.maharshi.bollywood_game_spring_boot.model.FriendVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import com.maharshi.bollywood_game_spring_boot.repository.FriendRepository;
import com.maharshi.bollywood_game_spring_boot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FriendsServiceImp implements FriendsService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Response findFriendsForPlayer() {
        PlayerVo playerVo = this.userService.findByUserName(this.userService.getCurrentUser().getUsername())
                .getPlayerVo();
        List<FriendVo> friendsVoList = this.friendRepository.findByPlayerVo(playerVo);
        List<FriendsDto> friendsDtoList = new ArrayList<>();

        for (FriendVo friendVo : friendsVoList) {
            // Check if the friend is online by querying Redis for the friend's player name
            boolean isOnline = redisTemplate.opsForValue().get(friendVo.getFriendInfo().getPlayerName()) != null;

            FriendsDto friendsDto = new FriendsDto(
                    friendVo.getId(),
                    friendVo.getFriendInfo().getPlayerName(),
                    friendVo.getMatchesWonTogether(),
                    friendVo.getMatchesPlayedTogether(),
                    friendVo.getFriendInfo().getMatchesPlayed(),
                    friendVo.getFriendInfo().getMatchesWon(),
                    isOnline // Set online status
            );
            friendsDtoList.add(friendsDto);
        }

        return new Response("success", friendsDtoList, true);
    }


    @Override
    public Response saveFriend(PlayerDto playerDto) {
        FriendVo friendVo;
        try {
            PlayerVo playerVo = this.userService.findByUserName(this.userService.getCurrentUser().getUsername())
                    .getPlayerVo();
            friendVo = new FriendVo();
            friendVo.setFriendInfo(new PlayerVo(playerDto.getId(), null, 0, 0));
            friendVo.setPlayerVo(playerVo);
            friendVo.setMatchesPlayedTogether(0);
            friendVo.setMatchesWonTogether(0);
            FriendVo friendVo1 = friendRepository.save(friendVo);
            return new Response("success", friendVo1, true);
        } catch (Exception e) {
            return new Response("failed", null, false);
        }
    }

    @Override
    public Response removefriend(int id) {
        try
        {
            FriendVo friendVo=new FriendVo();
            friendVo.setId(id);
            this.friendRepository.delete(friendVo);
            return new Response("success", null, true);
        }
        catch (Exception e)
        {
            return new Response("failed", null, false);
        }

    }

}
