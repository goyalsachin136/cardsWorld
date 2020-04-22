package com.example.accessingdatamysql.repository;

import com.example.accessingdatamysql.User;
import com.example.accessingdatamysql.model.Game;
import org.springframework.data.repository.CrudRepository;

public interface GameRepository extends CrudRepository<Game, Long> {

}