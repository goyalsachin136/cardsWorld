package com.example.accessingdatamysql.service;

public interface GamerService {

    String createGame(int numberOfPlayers, int numberOfCards);

    void setTrump(short trmup, String gameCode);

    void distributeCards(Integer numberOfCardsPerPlayer, String gameCode);
}
