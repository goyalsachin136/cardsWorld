package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.model.Player;

import java.util.List;

public interface PlayerService {
    void createPlayersInGame(String gameCode, short numberOfPlayers);

    String enterGame(short numericCode, String gameCode, String nickName);

    List<Player> getByGameCode(String gameCode);

    Player getByGameCodeAndNumericCode(String gameCode, short numericCode);

    void updatePlayers(List<Player> players);

    Player getByCode(String code);

    void removeCard(String code, short card);
}
