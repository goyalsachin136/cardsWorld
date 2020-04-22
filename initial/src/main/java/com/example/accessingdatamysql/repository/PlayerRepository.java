package com.example.accessingdatamysql.repository;

import com.example.accessingdatamysql.model.Game;
import com.example.accessingdatamysql.model.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlayerRepository extends CrudRepository<Player, Integer> {

    Player findByGameCodeAndNumericCode(String gameCode, short numericCode);

    List<Player> findByGameCode(String gameCode);
}
