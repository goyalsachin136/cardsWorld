package com.example.accessingdatamysql.service.impl;

import com.example.accessingdatamysql.model.CardSet;
import com.example.accessingdatamysql.repository.CardSetRepository;
import com.example.accessingdatamysql.service.CardSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardSetServiceImpl implements CardSetService {

    @Autowired
    private CardSetRepository cardSetRepository;

    @Override
    public CardSet updateCardSet(String gameCode, Long moveId) {
        CardSet cardSet = this.cardSetRepository.findByGameCodeAndIsCurrentSetIsTrue(gameCode);
        if (null == cardSet) {
            // new cardSet creation
            cardSet = CardSet.builder().gameCode(gameCode).moveIds(moveId.toString()).isCurrentSet(true).build();
        } else {
            cardSet.addMoveId(moveId);
        }
        return this.cardSetRepository.save(cardSet);
    }

    @Override
    public CardSet getActiveCardSet(String gameCode) {
        return this.cardSetRepository.findByGameCodeAndIsCurrentSetIsTrue(gameCode);
    }

    @Override
    public List<CardSet> getByGameCode(String gameCode) {
        return this.cardSetRepository.findByGameCode(gameCode);
    }

    @Override
    public void updateCardSet(CardSet cardSet) {
        this.cardSetRepository.save(cardSet);
    }
}
