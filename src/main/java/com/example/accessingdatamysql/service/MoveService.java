package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.model.Move;

import java.util.List;

public interface MoveService {
    Move createMove(String gameCode, Short card, String playerCode);

    List<Move> getByIds(List<Long> moveIds);

    List<Move> getByGameCode(String gameCode);
}
