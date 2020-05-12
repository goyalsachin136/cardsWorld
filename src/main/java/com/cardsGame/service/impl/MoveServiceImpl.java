package com.cardsGame.service.impl;

import com.cardsGame.model.Move;
import com.cardsGame.model.Player;
import com.cardsGame.repository.MoveRepository;
import com.cardsGame.service.MoveService;
import com.cardsGame.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoveServiceImpl implements MoveService {

    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private PlayerService playerService;

    @Override
    public Move createMove(String gameCode, Short card, String playerCode) {
        Player player = this.playerService.getByCode(playerCode);
        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        //these validation already covered in player service
        /*List<Short> cardsLeft = player.getAllCards();

        if (cardsLeft.isEmpty()) {
            throw new RuntimeException("No card left with player " + player.getNumericCode());
        }
        if (!cardsLeft.contains(card)) {
            throw new RuntimeException("Player " + player.getNumericCode() + " does not have card "
                    + getDisplayStringForCard(card));
        }*/
        return this.moveRepository.save(Move.builder().gameCode(gameCode).card(card).playerCode(playerCode).isActive(true).build());
    }

    @Override
    public List<Move> getByIds(List<Long> moveIds) {
        return this.moveRepository.findByIdIn(moveIds);
    }

    @Override
    public List<Move> getByGameCode(String gameCode) {
        return this.moveRepository.findByGameCode(gameCode);
    }
}
