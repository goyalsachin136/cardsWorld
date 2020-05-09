package com.example.accessingdatamysql.service.impl;

import com.example.accessingdatamysql.model.Move;
import com.example.accessingdatamysql.model.Player;
import com.example.accessingdatamysql.repository.MoveRepository;
import com.example.accessingdatamysql.service.MoveService;
import com.example.accessingdatamysql.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.accessingdatamysql.util.CommonUtil.getDisplayStringForCard;

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
