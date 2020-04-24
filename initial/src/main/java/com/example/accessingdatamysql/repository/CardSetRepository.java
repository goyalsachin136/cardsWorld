package com.example.accessingdatamysql.repository;

import com.example.accessingdatamysql.model.CardSet;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CardSetRepository extends CrudRepository<CardSet, Long> {
    CardSet findByGameCodeAndIsCurrentSetIsTrue(String gameCode);

    List<CardSet> findByGameCode(String gameCode);
}
