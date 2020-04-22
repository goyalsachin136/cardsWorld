package com.example.accessingdatamysql.service.impl;

import com.example.accessingdatamysql.UserRepository;
import com.example.accessingdatamysql.dto.GameStateDTO;
import com.example.accessingdatamysql.dto.PlayerGamePanelDTO;
import com.example.accessingdatamysql.model.CardSet;
import com.example.accessingdatamysql.model.Game;
import com.example.accessingdatamysql.model.Move;
import com.example.accessingdatamysql.model.Player;
import com.example.accessingdatamysql.repository.GameRepository;
import com.example.accessingdatamysql.service.CardSetService;
import com.example.accessingdatamysql.service.GamerService;
import com.example.accessingdatamysql.service.MoveService;
import com.example.accessingdatamysql.service.PlayerService;
import com.example.accessingdatamysql.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.smartcardio.Card;
import java.util.ArrayList;
import java.util.Collections;
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
        while (totalUndistributedCards != 0 && totalCardsToBeDistributed != 0) {
            numericIdToPlayerMap.get(currentPlayerWhichWillGetCard).addCard(undistributedCards.get(counter));

            currentPlayerWhichWillGetCard =  (short) (currentPlayerWhichWillGetCard != players.size()
                    ? currentPlayerWhichWillGetCard + 1 : 1);

            totalUndistributedCards--;
            totalCardsToBeDistributed--;
            counter++;
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

        this.playerService.removeCard(playerCode, card);

        Move move = this.moveService.createMove(gameCode, card, playerCode);

        CardSet cardSet = this.cardSetService.updateCardSet(gameCode, move.getId());

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
        Player winnerPlayer = this.playerService.getByGameCodeAndNumericCode(gameCode, winnerPlayerNumericCode);
        if (null == winnerPlayer) {
            throw new RuntimeException("Invalid winner numeric code");
        }
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code " + gameCode);
        }
        CardSet cardSet = this.cardSetService.getActiveCardSet(gameCode);
        if (null == cardSet) {
            throw new RuntimeException("No card set in progress. Player " + game.getCurrentPlayer() + " has to start game");
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

    @Override
    public GameStateDTO getGameState(String gameCode) {
        return null;
    }

    @Override
    public PlayerGamePanelDTO getPlayerStat(String playerCode) {
        return null;
    }
}
