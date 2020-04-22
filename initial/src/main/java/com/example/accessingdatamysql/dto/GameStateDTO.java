package com.example.accessingdatamysql.dto;

import com.example.accessingdatamysql.pojo.CardSetDTO;
import com.example.accessingdatamysql.pojo.PlayerInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDTO {
    private String gameCode;
    private short playerToMove;
    private String trumpCard; // to show if trump is open
    private String gameStateToDisplay; // admin distribute x number of cards, choose trump will be done by player itself,
    //admin distribute remaining cards,  %s player to move, admin  choose winner for this set,  game ended,

    private List<PlayerInfoDTO> playerInfoDTOS;
    private List<CardSetDTO> cardSetDTOS;
}
