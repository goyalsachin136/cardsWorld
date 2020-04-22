package com.example.accessingdatamysql.repository;

import com.example.accessingdatamysql.model.CardSet;
import org.springframework.data.repository.CrudRepository;

public interface CardSetRepository extends CrudRepository<CardSet, Long> {
    CardSet findByGameCodeAndIsCurrentSetIsTrue(String gameCode);
}
