package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.model.CardSet;

public interface CardSetService {
    CardSet updateCardSet(String gameCode, Long moveId);

    CardSet getActiveCardSet(String gameCode);

    void updateCardSet(CardSet cardSet);
}
