package com.example.accessingdatamysql.dto;

import com.example.accessingdatamysql.pojo.CardPOJO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerGamePanelDTO {
    private List<CardPOJO> cardPOJOList;
    private String trumpCard; //if set by player then it will be visible
    private String playerCode;
    private String openTrumpButton; // on click of this trump will be opened
    private short adminPlayerNumericCode; // by default 1

    //private List<List<CardPOJO>> setsWonList; do not include for now
    private short setsWon;
    private boolean isMyMove;
}
