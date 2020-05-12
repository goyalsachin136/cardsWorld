package com.cardsGame.service;

import com.cardsGame.model.CardSet;

import java.util.List;

public interface CardSetService {
    CardSet updateCardSet(String gameCode, Long moveId, Long bestMoveIdTillNow);

    CardSet getActiveCardSet(String gameCode);

    List<CardSet> getByGameCode(String gameCode);

    void updateCardSet(CardSet cardSet);
}
