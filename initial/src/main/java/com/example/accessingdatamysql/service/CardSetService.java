package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.model.CardSet;

import java.util.List;

public interface CardSetService {
    CardSet updateCardSet(String gameCode, Long moveId);

    CardSet getActiveCardSet(String gameCode);

    List<CardSet> getByGameCode(String gameCode);

    void updateCardSet(CardSet cardSet);
}
