package com.cardsGame.repository;

import com.cardsGame.model.Move;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MoveRepository extends CrudRepository<Move, Long> {

    List<Move> findByIdIn(List<Long> ids);

    List<Move> findByGameCode(String gameCode);
}

