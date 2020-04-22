package com.example.accessingdatamysql.repository;

import com.example.accessingdatamysql.model.Game;
import com.example.accessingdatamysql.model.Move;
import com.example.accessingdatamysql.model.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MoveRepository extends CrudRepository<Move, Long> {
}

