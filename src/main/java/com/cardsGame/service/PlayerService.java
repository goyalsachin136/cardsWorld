package com.cardsGame.service;

import com.cardsGame.model.Player;

import java.util.List;

public interface PlayerService {
    void createPlayersInGame(String gameCode, short numberOfPlayers);

    List<Player> getByGameCode(String gameCode);

    Player updatePlayer(Player player);

    Player getByGameCodeAndNumericCode(String gameCode, short numericCode);

    void updatePlayers(List<Player> players);

    Player getByCode(String code);

    void removeCard(String code, short card);
}
