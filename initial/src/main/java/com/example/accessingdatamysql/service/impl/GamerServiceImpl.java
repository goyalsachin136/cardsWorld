package com.example.accessingdatamysql.service.impl;

import com.example.accessingdatamysql.dto.GameStateDTO;
import com.example.accessingdatamysql.dto.PlayerGamePanelDTO;
import com.example.accessingdatamysql.enums.CardType;
import com.example.accessingdatamysql.model.CardSet;
import com.example.accessingdatamysql.model.Game;
import com.example.accessingdatamysql.model.Move;
import com.example.accessingdatamysql.model.Player;
import com.example.accessingdatamysql.pojo.CardPOJO;
import com.example.accessingdatamysql.pojo.CardSetDTO;
import com.example.accessingdatamysql.pojo.PlayerInfoDTO;
import com.example.accessingdatamysql.repository.GameRepository;
import com.example.accessingdatamysql.service.CardSetService;
import com.example.accessingdatamysql.service.GamerService;
import com.example.accessingdatamysql.service.MoveService;
import com.example.accessingdatamysql.service.PlayerService;
import com.example.accessingdatamysql.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GamerServiceImpl implements GamerService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MoveService moveService;

    @Autowired
    private CardSetService cardSetService;

    @Override
    public String createGame(int numberOfPlayers, int numberOfCards) {
        System.out.println("createGame numberOfCards " + numberOfCards + " numberOfPlayers " + numberOfPlayers);
        if (numberOfPlayers < 2 || numberOfPlayers > 4) {
            throw new RuntimeException("invalid player count " + numberOfPlayers);
        }

        if (numberOfCards > 52 || numberOfCards < 0) {
            throw new RuntimeException("invalid number of cards " + numberOfCards);
        }

        if (numberOfCards % numberOfPlayers != 0) {
            throw new RuntimeException(numberOfCards + " cards cannot be equally distributed among " + numberOfPlayers
                    + " players");
        }

        Game game = gameRepository.save(Game.builder()
                .code(CommonUtil.getSmallCapRandomString((short)6))
                .currentPlayer((short)1)
                .numberOfPlayers((short)numberOfPlayers)
                .numberOfCards((short)numberOfCards)
                .build()
        );

        this.playerService.createPlayersInGame(game.getCode(), game.getNumberOfPlayers());
        return game.getCode();
    }

    /**
     * leader can set trump and setLeader should work only if there is no move in progress
     * @param gameCode
     * @param playerCode
     */
    @Override
    public void setLeader(String gameCode, String playerCode) {
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code");
        }
        Player player = this.playerService.getByCode(playerCode);

        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        // TODO check if there is no set pending in CardSet
        game.setCurrentPlayer(player.getNumericCode());
        this.gameRepository.save(game);
    }

    @Override
    public void setTrump(short trump, String gameCode, String playerCode) {
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code");
        }
        if (null != game.getTrumpCard()) {
            throw new RuntimeException("trump already set");
        }
        if (trump <  0 || trump > 3) {
            throw new RuntimeException("invalid trump card");
        }
        game.setTrumpCard(trump);
        Player player = this.playerService.getByCode(playerCode);

        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        game.setTrumpSetByPlayerCode(player.getCode());
        game.setCurrentPlayer(player.getNumericCode());
        this.gameRepository.save(game);
    }

    @Override
    public void openTrump(String gameCode, String playerCode) {
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code");
        }
        if (!playerCode.equalsIgnoreCase(game.getTrumpSetByPlayerCode())) {
            throw new RuntimeException("Not privileged to open trump card");
        }
        if (Boolean.TRUE.equals(game.getIsTrumpOpen())) {
            throw new RuntimeException("Trump already open");
        }
        game.setIsTrumpOpen(true);
        this.gameRepository.save(game);
    }

    private Set<Short> getAlreadyDistributedCards(String gameCode) {
        List<Player> players = this.playerService.getByGameCode(gameCode);
        List<Short> alreadyDistributedCards = new ArrayList<>();
        for (Player player: players) {
            List<Short> distributedCards = player.getAllCards();
            if (!CollectionUtils.isEmpty(distributedCards)) {
                alreadyDistributedCards.addAll(distributedCards);
            }
        }
        return new HashSet<>(alreadyDistributedCards);
    }

    private List<Short> getUndistributedCards(Set<Short> alreadyDistributedCards) {
        List<Short> undistributedCards = new ArrayList<>();
        for (short card = 0; card < 52; card++) {
            if (!alreadyDistributedCards.contains(card)) {
                undistributedCards.add(card);
            }
        }
        return undistributedCards;
    }

    /**
     *
     * @param numberOfCardsPerPlayer if left empty it will distribute all remaining cards
     */
    @Override
    public void distributeCards(Integer numberOfCardsPerPlayer, String gameCode) {
        short leaderPlayer = (short) 1;
        //only if there is no move
        Set<Short> alreadyDistributedCards = getAlreadyDistributedCards(gameCode);
        List<Short> undistributedCards = getUndistributedCards(alreadyDistributedCards);

        if (undistributedCards.isEmpty()) {
            throw new RuntimeException("All cards already distributed");
        }
        Collections.shuffle(undistributedCards);

        List<Player> players = this.playerService.getByGameCode(gameCode);

        if (CollectionUtils.isEmpty(players)) {
            throw new RuntimeException("Invalid game code " + gameCode);
        }

        Map<Short, Player> numericIdToPlayerMap = players.stream()
                .collect(Collectors.toMap(Player::getNumericCode, Function.identity()));

        int totalUndistributedCards = undistributedCards.size();

        numberOfCardsPerPlayer = null != numberOfCardsPerPlayer ? numberOfCardsPerPlayer
                : totalUndistributedCards/players.size();
        int totalCardsToBeDistributed = numberOfCardsPerPlayer * players.size();

        short currentPlayerWhichWillGetCard =  (short)(leaderPlayer != players.size() ? leaderPlayer + 1 : 1);

        int counter = 0;
        if (totalCardsToBeDistributed > totalUndistributedCards) {
            throw new RuntimeException("Only " + totalUndistributedCards/players.size() +
                    " cards per person are left");
        }
        while (totalUndistributedCards != 0 && totalCardsToBeDistributed != 0) {
            numericIdToPlayerMap.get(currentPlayerWhichWillGetCard).addCard(undistributedCards.get(counter));

            currentPlayerWhichWillGetCard =  (short) (currentPlayerWhichWillGetCard != players.size()
                    ? currentPlayerWhichWillGetCard + 1 : 1);

            totalUndistributedCards--;
            totalCardsToBeDistributed--;
            counter++;
        }
        if (totalCardsToBeDistributed == totalUndistributedCards) {
            Game game = this.gameRepository.findByCode(gameCode);
            game.setCanGameBeStarted(true);
            this.gameRepository.save(game);
        }
        this.playerService.updatePlayers(players);
    }

    /**
     * Move validation should be there or make a method to undo move on approval of admin
     * @param card
     * @param playerCode
     * @param gameCode
     */
    @Override
    public void moveCard(short card, String playerCode, String gameCode) {
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code");
        }
        Player player = this.playerService.getByCode(playerCode);
        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        if (null == game.getCurrentPlayer()) {
            throw new RuntimeException("First decide winner of current game set");
        }
        if (!player.getNumericCode().equals(game.getCurrentPlayer())) {
            throw new RuntimeException("Invalid move. Currently player " + game.getCurrentPlayer() + " has to move");
        }

        CardSet cardSet = this.cardSetService.getActiveCardSet(gameCode);

        if (null != cardSet && !CollectionUtils.isEmpty(cardSet.getAllMoveIds())) {
            List<Move> moves = this.moveService.getByIds(cardSet.getAllMoveIds());
            Move move = moves.get(0);
            CardType requiredCardType = CommonUtil.getCardType(move.getCard());
            if (requiredCardType.equals(CommonUtil.getCardType(card))) {
                //okay
            } else {
                List<Short> allCards = player.getAllCards();
                CardType cardTypeFound = allCards.stream().map(cardNumber -> CommonUtil.getCardType(cardNumber))
                        .filter(cardTypeHere -> requiredCardType.equals(cardTypeHere)).findFirst().orElse(null);
                if (null != cardTypeFound) {
                    throw new RuntimeException("Please move " + cardTypeFound.name() + " Please move carefully ");
                }
            }
        }
        this.playerService.removeCard(playerCode, card);

        Move move = this.moveService.createMove(gameCode, card, playerCode);

        cardSet = this.cardSetService.updateCardSet(gameCode, move.getId());

        boolean isMoveEndMove = isMoveEndMoveOfSet(game.getNumberOfPlayers(), cardSet);

        if (isMoveEndMove) {
            game.setCurrentPlayer(null);
        } else {
            game.setCurrentPlayer(game.getNextPlayerToMove());
        }
        this.gameRepository.save(game);
    }

    // Only admin can choose winner for a set
    // be default admin is player with numeric code 1
    @Override
    public void chooseWinner(String adminPlayerCode, short winnerPlayerNumericCode, String gameCode) {
        Player player = this.playerService.getByCode(adminPlayerCode);
        if (null == player) {
            throw new RuntimeException("Invalid adminPlayerCode");
        }
        if (!player.getNumericCode().equals((short)1)) {
            throw new RuntimeException("Only admin can choose winner");
        }
        this.chooseWinner(gameCode, winnerPlayerNumericCode);
    }

    private void chooseWinner(String gameCode, short winnerPlayerNumericCode) {
        Player winnerPlayer = this.playerService.getByGameCodeAndNumericCode(gameCode, winnerPlayerNumericCode);
        if (null == winnerPlayer) {
            throw new RuntimeException("Invalid winner numeric code");
        }
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code " + gameCode);
        }
        CardSet cardSet = this.cardSetService.getActiveCardSet(gameCode);

        List<Move> moves = this.moveService.getByGameCode(gameCode);

        // if no active cardSet in progress and game is initialized
        if (null == cardSet && !moves.isEmpty()) {
            throw new RuntimeException("No card set in progress. Player " + game.getCurrentPlayer() + " has to start game");
        }

        // game initlialising condition
        if (null == cardSet && moves.isEmpty()) {
            game.setCurrentPlayer(winnerPlayerNumericCode);
            return;
        }

        if (!isMoveEndMoveOfSet(game.getNumberOfPlayers(), cardSet)) {
            throw new RuntimeException("Card set still in progress, admin cannot choose winner");
        }
        cardSet.setIsCurrentSet(false);
        cardSet.setWinnerPlayerCode(winnerPlayer.getCode());
        this.cardSetService.updateCardSet(cardSet);

        game.setCurrentPlayer(winnerPlayerNumericCode);
        this.gameRepository.save(game);
    }

    private static boolean isMoveEndMoveOfSet(int totalPlayers, CardSet cardSet) {
        if (cardSet.getAllMoveIds().size() == totalPlayers) {
            return true;
        }
        return false;
    }

    private List<PlayerInfoDTO> getFromCardSetListAndPlayersList(List<CardSet> cardSets, List<Player> players) {
       Map<String, Long> playerCodeToSetsWonCount = cardSets.stream()
               .filter(cardSet -> null != cardSet.getWinnerPlayerCode())
               .collect(Collectors.groupingBy(CardSet::getWinnerPlayerCode, Collectors.counting()));

        List<PlayerInfoDTO> playerInfoDTOS = new ArrayList<>();
       for (Player player: players) {
           PlayerInfoDTO playerInfoDTO = new PlayerInfoDTO();
           playerInfoDTO.setNumericCode(player.getNumericCode());
           playerInfoDTO.setSetsWon(playerCodeToSetsWonCount.containsKey(player.getCode())
                   ? playerCodeToSetsWonCount.get(player.getCode()).intValue() : 0);
           playerInfoDTO.setCardsLeft((short) player.getAllCards().size());
           playerInfoDTOS.add(playerInfoDTO);
       }
       return playerInfoDTOS;
    }

    @Override
    public GameStateDTO getGameState(String gameCode) {
        GameStateDTO gameStateDTO = new GameStateDTO();

        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code " + gameCode);
        }

        gameStateDTO.setGameCode(gameCode);
        gameStateDTO.setPlayerToMove(game.getCurrentPlayer());
        gameStateDTO.setTrumpCard(Boolean.TRUE.equals(game.getIsTrumpOpen())
                ? CardType.getFromIndex(game.getTrumpCard()).name() : null);

        List<Player> players = this.playerService.getByGameCode(gameCode);
        /*Map<String, Short> playerCodeToNumericCode = players.stream()
                .filter(code -> null != code)
                .collect(Collectors.toMap(Player::getCode, Player::getNumericCode));*/
        Map<String, Short> playerCodeToNumericCode = new HashMap<>();

        for (Player player: players) {
            if (null != player.getCode())
                playerCodeToNumericCode.put(player.getCode(), player.getNumericCode());
        }

        if (null != game.getTrumpSetByPlayerCode()) {
            gameStateDTO.setTrumpDeclaredBy(playerCodeToNumericCode.get(game.getTrumpSetByPlayerCode()));
        }
        gameStateDTO.setCanGameBeStarted(game.getCanGameBeStarted());
        //gameStateToDisplay


        List<CardSet> cardSets = this.cardSetService.getByGameCode(gameCode);

        gameStateDTO.setPlayerInfoDTOS(this.getFromCardSetListAndPlayersList(cardSets, players));




        CardSet activeCardSet = cardSets.stream().filter(cardSet -> Boolean.TRUE.equals(cardSet.getIsCurrentSet()))
                .findFirst().orElse(null);

        if (null == activeCardSet) {
            // waiting for 1st move of set
            //return gameStateDTO;
            // no active card set in progress
        } else if (null != activeCardSet && !activeCardSet.getAllMoveIds().isEmpty()) {
            // card set in progress
            List<Move> moves = this.moveService.getByIds(activeCardSet.getAllMoveIds());
            gameStateDTO.setCardSetDTOS(this.getFromMoves(moves, playerCodeToNumericCode));
        }

        //List<CardSet> cardSets = this.cardSetService.getByGameCode(gameCode);

        // no move still
        gameStateDTO.setGameStateToDisplay("");
        if (cardSets.isEmpty()) {

            if (players.get(0).getAllCards().size() != game.getNumberOfCards() / game.getNumberOfPlayers()) {
                gameStateDTO.setGameStateToDisplay("Admin distribute all cards please\n");
            }

            if (null == game.getTrumpCard()) {
                gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay().concat(" Choose trump \n"));
                return gameStateDTO;
            }

            if (null == game.getCurrentPlayer()) {
                gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay().concat(" set leader for game start\n"));
            }

            if (null != game.getCurrentPlayer()) {
                gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay()
                        .concat(" Player " + game.getCurrentPlayer() + " it's your move\n"));
            }
        } else if (null != game.getCurrentPlayer()) {
            gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay()
                    .concat(" Player " + game.getCurrentPlayer() + " it's your move\n"));
        } else if (null == game.getCurrentPlayer()) {
            gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay()
                    .concat(" Admin  choose winner for current set \n"));
        }
        return gameStateDTO;
    }

    private List<CardSetDTO> getFromMoves(List<Move> moves, Map<String, Short> playerCodeToNumericCode) {
        List<CardSetDTO> cardSetDTOS = new ArrayList<>();
        for (Move move: moves) {
            cardSetDTOS.add(new CardSetDTO(CommonUtil.getDisplayStringForCard(move.getCard()),
                    playerCodeToNumericCode.get(move.getPlayerCode()), CommonUtil.getCardType(move.getCard()))
            );
        }
        return cardSetDTOS;
    }

    @Override
    public PlayerGamePanelDTO getPlayerStat(String playerCode) {
        System.out.println("getPlayerStat "+  playerCode);
        PlayerGamePanelDTO playerGamePanelDTO = new PlayerGamePanelDTO();
        Player player = this.playerService.getByCode(playerCode);
        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        playerGamePanelDTO.setPlayerNumericCode(player.getNumericCode());
        List<CardPOJO> cardPOJOS = player.getAllCards().stream()
                .map(card -> new CardPOJO(card, CommonUtil.getCardType(card),
                        CommonUtil.getDisplayStringForCard(card)))
                .collect(Collectors.toList());
        Map<CardType, List<CardPOJO>> cardTypeListMap = cardPOJOS.stream()
                .collect(Collectors.groupingBy(cardPOJO -> cardPOJO.getCardType(),
                        Collectors.toList()));
        cardTypeListMap.values().forEach(list -> list.sort(Comparator.comparing(CardPOJO::getCard)));

        playerGamePanelDTO.setCardTypeToCardDisplayStringMap(cardTypeListMap);

        Game game = this.gameRepository.findByCode(player.getGameCode());
        if (playerCode.equals(game.getTrumpSetByPlayerCode())) {
            playerGamePanelDTO.setTrumpCard(CardType.getFromIndex(game.getTrumpCard()).name());
        }
        playerGamePanelDTO.setPlayerCode(playerCode);
        playerGamePanelDTO.setOpenTrumpButton(!Boolean.TRUE.equals(game.getIsTrumpOpen()));

        List<CardSet> cardSets = this.cardSetService.getByGameCode(player.getGameCode());

        playerGamePanelDTO.setSetsWon((short) cardSets.stream()
                .filter(cardSet -> playerCode.equals(cardSet.getWinnerPlayerCode())).count());
        playerGamePanelDTO.setAdminPlayerNumericCode((short) 1);
        return playerGamePanelDTO;
    }
}
