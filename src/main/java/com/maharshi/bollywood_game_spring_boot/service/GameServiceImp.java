package com.maharshi.bollywood_game_spring_boot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maharshi.bollywood_game_spring_boot.dto.*;
import com.maharshi.bollywood_game_spring_boot.model.GameVo;
import com.maharshi.bollywood_game_spring_boot.model.InGamePlayerVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import com.maharshi.bollywood_game_spring_boot.repository.GameRepository;
import com.maharshi.bollywood_game_spring_boot.repository.InGamePLayerRepository;
import com.maharshi.bollywood_game_spring_boot.repository.PlayerRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@Transactional
public class GameServiceImp implements GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private InGamePLayerRepository inGamePLayerRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeminiAiService geminiAiService;


    @Override
    public Response startGame(GameDto gameDto) {
        PlayerVo hostPlayerVo = this.playerRepository.findById(gameDto.getHostPlayerId());
        GameVo gameVo = new GameVo(0
                , hostPlayerVo
                , null
                , "0"
                , null
                , null
                , null
                , null
                , 9
                , new ArrayList<>()
                , 1
                , gameDto.getFriendsDtoList().size() + 1
                , hostPlayerVo.getPlayerName());
        List<InGamePlayerDto> inGamePlayerDtoList = new ArrayList<>();
        try {
            gameVo = gameRepository.save(gameVo);

            List<FriendsDto> friendsDtoList = gameDto.getFriendsDtoList();

//            This preparation is for host player.
            hostPlayerVo.setGameVo(gameVo);
            hostPlayerVo = playerRepository.save(hostPlayerVo);

            InGamePlayerVo inGamePlayerVo = new InGamePlayerVo(0, 0,
                    hostPlayerVo, gameVo, 1, "Host");
            inGamePlayerVo = inGamePLayerRepository.save(inGamePlayerVo);

            InGamePlayerDto inGamePlayerDto = new InGamePlayerDto(inGamePlayerVo.getId(),
                    inGamePlayerVo.getGamePoints(), hostPlayerVo.getPlayerName()
                    , gameVo.getId(), 1, "Host");
            inGamePlayerDtoList.add(inGamePlayerDto);


//            This is game preparation for another players.
            int sequence = 2;
            for (FriendsDto friendsDto : friendsDtoList) {
//                Saving the inGamePlayerVo in database
                PlayerVo playerVo = playerRepository.findByPlayerName(friendsDto.getFriendName());
                inGamePlayerVo = new InGamePlayerVo(0, 0, playerVo
                        , gameVo, sequence
                        , "Requested");
                inGamePlayerVo = inGamePLayerRepository.save(inGamePlayerVo);
                inGamePlayerDto = new InGamePlayerDto(inGamePlayerVo.getId(),
                        inGamePlayerVo.getGamePoints(), playerVo.getPlayerName()
                        , gameVo.getId(), sequence, "Requested");
                inGamePlayerDtoList.add(inGamePlayerDto);
                sequence++;
            }

//            Preparing GameDto
            gameDto.setInGamePlayerDtoList(inGamePlayerDtoList);
            gameDto.setId(gameVo.getId());
            gameDto.setHostPlayerName(hostPlayerVo.getPlayerName());
            gameDto.setChanceLeft(9);
            gameDto.setCurrentPlayerChanceIndex(1);
            gameDto.setCurrentChancePlayerName(hostPlayerVo.getPlayerName());
            gameDto.setFriendsDtoList(null);
            gameDto.setPreviousGuesses(gameVo.getPreviousGuesses());
            gameDto.setPauseSeconds(gameVo.getPauseSeconds());
            gameDto.setTotalPlayer(gameVo.getTotalPlayer());


//            Saving Game In Redis For Caching
            redisService.set(String.valueOf(gameDto.getId()), gameDto, null);

//            Updating the hostPlayer Status
            playerService.updatePlayerStatus(hostPlayerVo.getPlayerName(), "Host",
                    String.valueOf(gameDto.getId()));

//            Sending Requests to the players on the general stream !!
            for (InGamePlayerDto inGamePlayerDto1 : gameDto.getInGamePlayerDtoList()) {
                //                Use kafka here
                if (inGamePlayerDto1.getPlayerName().equals(gameDto.getHostPlayerName())) {
                    continue;
                }
                SocketResponse response = new
                        SocketResponse(inGamePlayerDto1
                        .getPlayerName(), gameDto, "gameRequest");
                messagingTemplate.convertAndSend("/topic/general", response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Response("success", gameDto, true);
    }

    @Override
    public Response getGameDetails() {

        try {
//            finding current game for player.
            PlayerVo playerVo = this.playerService.getCurrentPlayer();
            if (playerVo.getGameVo() == null) {

                return new Response("no game found for player", null, false);
            }

//            preparing gameDto if there is any current game.
            GameVo gameVo = playerVo.getGameVo();
//            getting data from redis
            GameDto gameDtoFromRed = redisService
                    .get(String.valueOf(gameVo.getId()), GameDto.class);

//            checking if data is available in redis or  not
            if (gameDtoFromRed != null) {
                String actualMovieName = gameDtoFromRed.getActualMovieName();
                gameDtoFromRed.setActualMovieName(null);
                String currentPlayerName = playerService.getCurrentPlayer().getPlayerName();
                int playersOnline = 0;
                for (InGamePlayerDto inGamePlayerDto : gameDtoFromRed.getInGamePlayerDtoList()) {
                    if (inGamePlayerDto.getPlayerName().equals(currentPlayerName)) {
                        inGamePlayerDto
                                .setStatus(currentPlayerName
                                        .equals(gameDtoFromRed.getHostPlayerName())
                                        ? "Host" : "In Game");
                    }

                    if ("Host".equals(inGamePlayerDto.getStatus())
                            || "In Game".equals(inGamePlayerDto.getStatus())) {
                        playersOnline++;
                    }
                }

                if (playersOnline >= 2 && !gameDtoFromRed.getPauseSeconds().equals("0")) {
                    long newRoundOutTime = System.currentTimeMillis() + Long.parseLong(gameDtoFromRed
                            .getPauseSeconds());
                    gameDtoFromRed.setRoundTimeOut(String.valueOf(newRoundOutTime));
                    gameDtoFromRed.setPauseSeconds("0");
                    SocketResponse response1 = new SocketResponse("GameRelease"
                            , gameDtoFromRed
                            , "updateGame");
                    messagingTemplate
                            .convertAndSend("/topic/game/"
                                    + gameDtoFromRed.getId(), response1);
                }
                playerService.updatePlayerStatus(currentPlayerName,
                        currentPlayerName
                                .equals(gameDtoFromRed
                                        .getHostPlayerName()) ? "Host" : "In Game",
                        String.valueOf(gameVo.getId()));
                gameDtoFromRed.setActualMovieName(actualMovieName);
                saveAndTransferGameDto(gameDtoFromRed, false);
                return new Response("success", gameDtoFromRed, true);
            }
//            fetching data from redis if the data is not available
            List<InGamePlayerVo> inGamePlayerVoList = this
                    .inGamePLayerRepository.findByGameVo(gameVo);
            List<InGamePlayerDto> inGamePlayerDtoList = new ArrayList<>();
            for (InGamePlayerVo inGamePlayerVo : inGamePlayerVoList) {
                InGamePlayerDto inGamePlayerDto = new InGamePlayerDto(inGamePlayerVo.getId(),
                        inGamePlayerVo.getGamePoints()
                        , inGamePlayerVo.getPlayerVo().getPlayerName()
                        , gameVo.getId(),
                        inGamePlayerVo.getSequenceNumber(),
                        playerService
                                .getPlayerStatus(inGamePlayerVo.getPlayerVo().getPlayerName())
                );
                inGamePlayerDtoList.add(inGamePlayerDto);
            }
            GameDto gameDto = new GameDto(gameVo.getId(),
                    gameVo.getHostPlayerVo().getId()
                    , gameVo.getHostPlayerVo().getPlayerName()
                    , null
                    , inGamePlayerDtoList
                    , gameVo.getCurrentPlayerChanceIndex()
                    , gameVo.getCurrentChancePlayerName()
                    , gameVo.getMovieName()
                    , gameVo.getActualMovieName()
                    , gameVo.getUniqueAlphabets()
                    , gameVo.getRoundTimeOut()
                    , gameVo.getHint()
                    , gameVo.getPauseSeconds()
                    , gameVo.getChanceLeft()
                    , gameVo.getPreviousGuesses()
                    , gameVo.getTotalPlayer()
                    , gameVo.getTotalPlayer());

//            saving data in redis sever
            redisService.set(String.valueOf(gameDto.getId()), gameDto, null);
//            Updating the player status
            String currentPlayer = playerService.getCurrentPlayer().getPlayerName();
            playerService.updatePlayerStatus(currentPlayer,
                    currentPlayer
                            .equals(gameDtoFromRed.getHostPlayerName()) ? "Host" : "In Game",
                    String
                            .valueOf(gameDto.getId()));
            gameDto.setActualMovieName(null);
            return new Response("success", gameDto, true);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("no player found", null, false);
        }


    }

    @Override
    public Response quitGame(GameDto gameDto) {

        return null;
    }

    @Override
    public Response endGame(GameDto gameDto) {
        try {
//            Getting the Game details from the redis.
            gameDto = this.redisService.get(String.valueOf(gameDto.getId()), GameDto.class);

//            Updating the GameVo
            PlayerVo hostPlayerVo = new PlayerVo();
            hostPlayerVo.setId(gameDto.getHostPlayerId());
            hostPlayerVo.setPlayerName(gameDto.getHostPlayerName());
            GameVo gameVo = new GameVo(gameDto.getId()
                    , hostPlayerVo
                    , gameDto.getRoundTimeOut()
                    , gameDto.getPauseSeconds()
                    , gameDto.getMovieName()
                    , gameDto.getActualMovieName()
                    , gameDto.getUniqueAlphabets()
                    , gameDto.getHint()
                    , gameDto.getChanceLeft()
                    , gameDto.getPreviousGuesses()
                    , gameDto.getCurrentPlayerChanceIndex()
                    , gameDto.getTotalPlayer()
                    , gameDto.getCurrentChancePlayerName());
            this.gameRepository.save(gameVo);
            for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
//               Updating the inGamePlayerVo and playerVo
                InGamePlayerVo inGamePlayerVo = this.inGamePLayerRepository
                        .findById(inGamePlayerDto.getId());
                PlayerVo playerVo = this.playerRepository
                        .findById(inGamePlayerVo.getPlayerVo().getId());
                inGamePlayerVo.setGamePoints(inGamePlayerDto.getGamePoints());
                playerVo.setGameVo(null);
                playerVo.setPoints(playerVo.getPoints() + inGamePlayerDto.getGamePoints());
                this.playerService.updatePlayerStatus(playerVo.getPlayerName()
                        , "Online", String.valueOf(gameDto.getId()));
                this.playerRepository.save(playerVo);
                this.inGamePLayerRepository.save(inGamePlayerVo);
            }
            SocketResponse response1 = new SocketResponse("GameEnded", gameDto
                    , "GameOver");
            messagingTemplate.convertAndSend("/topic/game/" + gameDto.getId()
                    , response1);


            redisService.delete(String.valueOf(gameDto.getId()));
        } catch (Exception e) {
            log.error(e.toString());
        }
        return new Response("success", gameDto, true);
    }

    @Override
    public Response gameRequestReply(PlayerStatusDto playerStatusDto) {
        GameDto gameDto = redisService
                .get(String.valueOf(playerStatusDto.getGameId()), GameDto.class);
        if (playerStatusDto.getStatus().equals("Accepted")) {
            for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
                if (inGamePlayerDto.getPlayerName().equals(playerStatusDto.getPlayerName())) {
//             Changing the status firstly in cache memory
                    inGamePlayerDto.setStatus("In Game");

//             Updating the playerVo for Acceptance or if denied
                    PlayerVo playerVo = playerRepository
                            .findByPlayerName(playerStatusDto.getPlayerName());
                    GameVo gameVo = this.gameRepository.findById(gameDto.getId());
                    playerVo.setGameVo(gameVo);
                    playerVo = this.playerRepository.save(playerVo);
//                Additionally changing the status in the inGamePlayer Entity in database
                    InGamePlayerVo inGamePlayerVo = this.inGamePLayerRepository
                            .findById(inGamePlayerDto.getId());
                    inGamePlayerVo.setJoinedStatus("Accepted");
                    this.inGamePLayerRepository.save(inGamePlayerVo);
//                Maintaining the status of the player in cache memory !
                    this.playerService.updatePlayerStatus(playerVo.getPlayerName()
                            , "In Game", String.valueOf(gameVo.getId()));
                    break;
                }
            }
            this.redisService.set(String.valueOf(gameDto.getId()), gameDto, null);
        } else {

            for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
                if (inGamePlayerDto.getPlayerName().equals(playerStatusDto.getPlayerName())) {
//             Changing the status firstly in cache memory
                    inGamePlayerDto.setStatus("Denied");

//                Additionally changing the status in the inGamePlayer Entity in database
                    GameVo gameVo = new GameVo();
                    gameVo.setId(gameDto.getId());
//                Additionally changing the status in the inGamePlayer Entity in database
                    InGamePlayerVo inGamePlayerVo = this.inGamePLayerRepository
                            .findById(inGamePlayerDto.getId());
                    inGamePlayerVo.setJoinedStatus("Denied");
                    this.inGamePLayerRepository.save(inGamePlayerVo);
//                    Letting the other player know about the accept or reject status !
                    this.playerService.updatePlayerStatus(inGamePlayerDto.getPlayerName()
                            , "Denied", String.valueOf(gameVo.getId()));
                    break;
                }
            }
            this.redisService.set(String.valueOf(gameDto.getId()), gameDto, null);
        }

        return new Response("success", playerStatusDto, true);
    }

    @Override
    public Response reSendRequest(PlayerStatusDto playerStatusDto) {
        // Retrieve the game from Redis
        GameDto gameDto = redisService.get(playerStatusDto.getGameId(), GameDto.class);
        if (gameDto == null) {
            throw new IllegalArgumentException("Game not found for ID: " + playerStatusDto.getGameId());
        }

        for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
            if (inGamePlayerDto.getPlayerName().equals(playerStatusDto.getPlayerName())) {
                inGamePlayerDto.setStatus("Requested");
                InGamePlayerVo inGamePlayerVo = this.inGamePLayerRepository.findById(inGamePlayerDto.getId());
                inGamePlayerVo.setJoinedStatus("Requested");
                this.inGamePLayerRepository.save(inGamePlayerVo);
                break;
            }
        }


        // Send WebSocket response
        SocketResponse response = new SocketResponse(
                playerStatusDto.getPlayerName(),
                gameDto,
                "gameRequest"
        );
        messagingTemplate.convertAndSend("/topic/general", response);

        // Update Redis
        redisService.set(String.valueOf(gameDto.getId()), gameDto, null);


        return new Response("success", playerStatusDto, true);
    }

    @Override
    public void askingGuessingStatus(SocketRequest socketRequest) {
        PlayerStatusDto playerStatusDto = objectMapper
                .convertValue(socketRequest.getBody(), PlayerStatusDto.class);
        SocketResponse response = new SocketResponse(socketRequest.getMessage()
                , playerStatusDto
                , socketRequest.getType());
        messagingTemplate
                .convertAndSend("/topic/game/"
                                + String.valueOf(playerStatusDto.getGameId())
                        , response);
    }


    @Override
    public void changePlayerInGameStatus(PlayerStatusDto playerStatusDto) {
        GameDto gameDto = (GameDto) redisService
                .get(String.valueOf(playerStatusDto.getGameId()),
                        GameDto.class);
        List<InGamePlayerDto> inGamePlayerDtoList = gameDto.getInGamePlayerDtoList();

        for (InGamePlayerDto inGamePlayerDto : inGamePlayerDtoList) {
            if (playerStatusDto.getPlayerName().equals(inGamePlayerDto.getPlayerName())) {
                playerStatusDto.setStatus(playerStatusDto.getStatus());
                break;
            }
        }
        playerService.updatePlayerStatus(playerStatusDto.getPlayerName(), "In Game",
                String.valueOf(gameDto.getId()));
        redisService.set(String.valueOf(gameDto.getId()), gameDto, null);
    }

    @Override
    public void movieNameAsked(AskingAndGuessingDto askingAndGuessingDto) {
        String actualMovieName = askingAndGuessingDto.getAskedMovieName();
        String timeStampAfterFiveMins = String.valueOf(System.currentTimeMillis() + (120 * 60 * 1000));


        char[] movieNameCharArray = actualMovieName.toCharArray();

        List<Character> movieName = new ArrayList<>();

        for (char ch : movieNameCharArray) {
            if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u'
                    || ch == 'A' || ch == 'E' || ch == 'I' || ch == 'O' || ch == 'U') {
                movieName.add(ch);
            } else if (ch == ' ') {
                movieName.add('|');
            } else {
                movieName.add('_');
            }
        }

        // Convert movieName list to string
        StringBuilder movieNameString = new StringBuilder();
        for (Character ch : movieName) {
            movieNameString.append(ch);
        }

        GameDto gameDto = (GameDto) redisService.get(String.valueOf(askingAndGuessingDto.getId())
                , GameDto.class);
        gameDto.setMovieName(movieNameString.toString());
        gameDto.setActualMovieName(actualMovieName);
        gameDto.setRoundTimeOut(timeStampAfterFiveMins);
        this.redisService.set(String.valueOf(gameDto.getId()), gameDto, null);

        askingAndGuessingDto.setAskedMovieName(movieNameString.toString());
        askingAndGuessingDto.setTimeStamp(timeStampAfterFiveMins);

        SocketResponse response = new SocketResponse("movieNameAsked"
                , askingAndGuessingDto
                , "movieNameUpdated");
        messagingTemplate
                .convertAndSend("/topic/game/"
                        + askingAndGuessingDto.getId(), response);

        for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
            PlayerStatusDto playerStatusDto = new PlayerStatusDto(inGamePlayerDto.getPlayerName()
                    , null, String.valueOf(askingAndGuessingDto.getId()));
            if (inGamePlayerDto.getPlayerName().equals(askingAndGuessingDto.getAskerPlayerName())) {
                playerStatusDto.setStatus("asked");

            } else {
                playerStatusDto.setStatus("will guess");

            }
            SocketResponse response1 = new SocketResponse("AskingGuessingStatus"
                    , playerStatusDto
                    , "playerInGameStatus");
            messagingTemplate
                    .convertAndSend("/topic/game/"
                            + playerStatusDto.getGameId(), response1);
        }
    }


    @Override
    public void guessMovieName(AskingAndGuessingDto askingAndGuessingDto) {

        String guessedWordOrLetter = askingAndGuessingDto.getGuessedWordOrLetter();
        String playerName = askingAndGuessingDto.getGuesserPlayerName();

        GameDto gameDto = (GameDto) redisService.get(String.valueOf(askingAndGuessingDto.getId()), GameDto.class);

        String movieName = gameDto.getMovieName();
        String actualMovieName = gameDto.getActualMovieName();

        if (guessedWordOrLetter.length() == 1) {
            char ch = guessedWordOrLetter.charAt(0);
            long count = actualMovieName.chars().filter(c -> c == ch).count();
            if (count > 0) {
                for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
                    if (inGamePlayerDto.getPlayerName().equals(playerName)) {
                        inGamePlayerDto.setGamePoints((int) (inGamePlayerDto.getGamePoints() + count));
                    }
                }
                StringBuilder sb = new StringBuilder(movieName);

                for (int i = 0; i < actualMovieName.length(); i++) {
                    if (actualMovieName.charAt(i) == ch) {
                        sb.setCharAt(i, ch);
                    }
                }
                movieName = sb.toString();
                gameDto.setMovieName(movieName);
                long underScoreLeft = gameDto.getMovieName().chars().filter(c -> c == '_').count();
                if (underScoreLeft == 0) {
                    wrapThisRound(gameDto);
                }

            } else {
                gameDto.setChanceLeft(gameDto.getChanceLeft() - 1);
                List<String> previousGuesses = gameDto.getPreviousGuesses();
                previousGuesses.add(guessedWordOrLetter);
            }
        } else {
            if (guessedWordOrLetter.equals(actualMovieName)) {
                long count = movieName.chars().filter(c -> c == '_').count();
                for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
                    if (inGamePlayerDto.getPlayerName().equals(playerName)) {
                        inGamePlayerDto.setGamePoints((int) (inGamePlayerDto.getGamePoints() + count));
                    }
                }
                gameDto.setMovieName(actualMovieName);
                wrapThisRound(gameDto);
            } else {
                gameDto.setChanceLeft(gameDto.getChanceLeft() - 1);
                List<String> previousGuesses = gameDto.getPreviousGuesses();
                previousGuesses.add(guessedWordOrLetter);
            }
        }

        if (gameDto.getChanceLeft() <= 0) {
            long underScoreLeft = gameDto.getMovieName().chars().filter(c -> c == '_').count();
            for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
                if (inGamePlayerDto.getPlayerName().equals(gameDto.getCurrentChancePlayerName())) {
                    inGamePlayerDto.setGamePoints((int) (inGamePlayerDto.getGamePoints() + underScoreLeft));
                }
            }
            wrapThisRound(gameDto);

            return;
        }

        saveAndTransferGameDto(gameDto, false);
    }

    public void wrapThisRound(GameDto gameDto) {
        SocketResponse response1 = new SocketResponse("MovieNameDeclared"
                , gameDto.getActualMovieName()
                , "movieNameDeclared");
        messagingTemplate
                .convertAndSend("/topic/game/"
                        + gameDto.getId(), response1);
        gameDto.setRoundTimeOut(null);
        gameDto.setMovieName(null);
        gameDto.setActualMovieName(null);
        gameDto.setUniqueAlphabets(null);
        gameDto.setHint(null);
        gameDto.setPauseSeconds("0");
        gameDto.setPreviousGuesses(new ArrayList<String>());
        gameDto.setChanceLeft(9);
        int numberOfPlayers = gameDto.getTotalPlayer();
        int nextChance = gameDto.getCurrentPlayerChanceIndex() + 1;
        if (numberOfPlayers < nextChance) {
            nextChance = 1;
        }
        gameDto.setCurrentPlayerChanceIndex(nextChance);
        for (InGamePlayerDto inGamePlayerDto : gameDto.getInGamePlayerDtoList()) {
            if (inGamePlayerDto.getSequenceNumber() == nextChance) {
                gameDto.setCurrentChancePlayerName(inGamePlayerDto.getPlayerName());
                break;
            }
        }
        saveAndTransferGameDto(gameDto, false);
    }

    @Override
    public void completeThisRound(SocketRequest socketRequest) {
        GameDto gameDto = this.objectMapper
                .convertValue(socketRequest.getBody(), GameDto.class);
        gameDto = this.redisService.get(String.valueOf(gameDto.getId()), GameDto.class);
//        Write  logic to give points to asker !!
        long count = gameDto.getMovieName().chars().filter(c -> c == '_').count();

        wrapThisRound(gameDto);
    }

    @Override
    public Response getHintForMovie(GameDto gameDto) {
        String movieName = gameDto.getActualMovieName();
        log.info(movieName);
        try {
            String gemResponse = this.geminiAiService.askGemini(
                    "Provide a unique and concise hint for the Bollywood movie \"" + movieName + "\". " +
                            "Each hint should differ from previous ones. The hint can be an actor, actress, or song " +
                            "from the movie. If no information is available, respond with 'No hint available'."
            );

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(gemResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            // Extract the text value
            String hintText = textNode.asText();
            return new Response("success", hintText, true);

        } catch (Exception e) {
            return new Response("failed", "AI Unavailable", true);
        }
    }

    @Override
    public Response getHintForMovieFromChatGpt(GameDto gameDto) {
        String movieName = gameDto.getActualMovieName();

        try {
            String gemResponse = this.geminiAiService.askChatGpt(
                    "Provide a unique and concise hint for the movie \"" + movieName + "\". " +
                            "The hint can be a tiny description about the movie. " +
                            "If no information about the movie is available, respond with 'No hint available'."
            );


            // Parse JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(gemResponse);

            // Navigate to choices[0].message.content
            String hint = rootNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return new Response("success", hint, true);

        } catch (Exception e) {
            log.error("Error while getting hint: {}", e.getMessage(), e);
            return new Response("failed", "AI Unavailable", true);
        }
    }

    @Override
    public void passHint(SocketRequest socketRequest) {
        GameDto gameDto = this.objectMapper
                .convertValue(socketRequest.getBody(), GameDto.class);
        String hint = gameDto.getHint();
        gameDto = this.redisService.get(String.valueOf(gameDto.getId()), GameDto.class);
        gameDto.setHint(hint);
        redisService.set(String.valueOf(gameDto.getId()), gameDto, null);
        SocketResponse response1 = new SocketResponse("HintPassed"
                , hint
                , "hintUpdated");
        gameDto.setActualMovieName(null);
        messagingTemplate
                .convertAndSend("/topic/game/"
                        + gameDto.getId(), response1);
    }


    public void saveAndTransferGameDto(GameDto gameDto, boolean inDataBase) {
        if (inDataBase) {

        } else {
            redisService.set(String.valueOf(gameDto.getId()), gameDto, null);
            gameDto.setActualMovieName(null);
            SocketResponse response1 = new SocketResponse("SomeoneJustGussed"
                    , gameDto
                    , "updateGame");
            gameDto.setActualMovieName(null);
            messagingTemplate
                    .convertAndSend("/topic/game/"
                            + gameDto.getId(), response1);
        }
    }


}
