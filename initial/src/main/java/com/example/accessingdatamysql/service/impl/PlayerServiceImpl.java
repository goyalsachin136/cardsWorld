package com.example.accessingdatamysql.service.impl;

import com.example.accessingdatamysql.model.Player;
import com.example.accessingdatamysql.repository.PlayerRepository;
import com.example.accessingdatamysql.service.PlayerService;
import com.example.accessingdatamysql.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public void createPlayersInGame(String gameCode, short numberOfPlayers) {
        List<Player> playerList = new ArrayList();
        for (int i = 1; i <= numberOfPlayers; i++) {
            playerList.add(Player.builder().numericCode((short) i).gameCode(gameCode).build());
        }
        this.playerRepository.saveAll(playerList);
    }

    @Override
    public String enterGame(short numericCode, String gameCode) {
        Player player = this.playerRepository.findByGameCodeAndNumericCode(gameCode, numericCode);
        if (null == player) {
            throw new RuntimeException("Invalid game code " + gameCode + " or invalid numericCode " + numericCode);
        } else if (null != player.getCode()) {
            throw new RuntimeException("Player already entered for this id " + numericCode);
        } else {
            player.setCode(CommonUtil.getSmallCapRandomString((short)6));
            return playerRepository.save(player).getCode();
        }
    }

    @Override
    public List<Player> getByGameCode(String gameCode) {
        return playerRepository.findByGameCode(gameCode);
    }

    @Override
    public void updatePlayers(List<Player> players) {
        if (CollectionUtils.isEmpty(players)) {
            return;
        }
        this.playerRepository.saveAll(players);
    }
}
