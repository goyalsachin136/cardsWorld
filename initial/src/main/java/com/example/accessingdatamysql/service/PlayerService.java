package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.model.Player;

import java.util.List;

public interface PlayerService {
    void createPlayersInGame(String gameCode, short numberOfPlayers);

    String enterGame(short numericCode, String gameCode);

    List<Player> getByGameCode(String gameCode);

    void updatePlayers(List<Player> players);
}
