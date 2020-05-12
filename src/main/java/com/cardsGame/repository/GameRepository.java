package com.cardsGame.repository;

import com.cardsGame.model.Game;
import org.springframework.data.repository.CrudRepository;

public interface GameRepository extends CrudRepository<Game, Long> {
    Game findByCode(String code);
}
