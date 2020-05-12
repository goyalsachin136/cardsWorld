package com.cardsGame.repository;

import com.cardsGame.model.CardSet;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CardSetRepository extends CrudRepository<CardSet, Long> {
    CardSet findByGameCodeAndIsCurrentSetIsTrue(String gameCode);

    List<CardSet> findByGameCode(String gameCode);
}
