package com.cardsGame.repository;

import com.cardsGame.model.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlayerRepository extends CrudRepository<Player, Integer> {

    Player findByGameCodeAndNumericCode(String gameCode, short numericCode);

    List<Player> findByGameCode(String gameCode);

    Player findByCode(String code);
}
