package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.model.Move;

public interface MoveService {
    Move createMove(String gameCode, Short card, String playerCode);
}
