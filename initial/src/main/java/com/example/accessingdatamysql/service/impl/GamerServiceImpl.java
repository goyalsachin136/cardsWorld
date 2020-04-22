package com.example.accessingdatamysql.service.impl;

import com.example.accessingdatamysql.UserRepository;
import com.example.accessingdatamysql.model.Game;
import com.example.accessingdatamysql.model.Player;
import com.example.accessingdatamysql.repository.GameRepository;
import com.example.accessingdatamysql.service.GamerService;
import com.example.accessingdatamysql.service.PlayerService;
import com.example.accessingdatamysql.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    @Override
    public void setTrump(short trmup, String gameCode) {

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
}
