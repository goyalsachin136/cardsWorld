package com.example.accessingdatamysql.dto;

import com.example.accessingdatamysql.enums.CardType;
import com.example.accessingdatamysql.pojo.CardPOJO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerGamePanelDTO {
    Map<CardType, List<CardPOJO>> cardTypeToCardDisplayStringMap; // cardType to sorted list
    //private List<CardPOJO> cardPOJOList;
    private String trumpCard; //if set by player then it will be visible
    private Short playerNumericCode;
    private String playerCode;
    private Boolean openTrumpButton; // on click of this trump will be opened -- will be shown to player
    private String distributeCardsButton; // will have a option to choose number of cards per person and will show that no cards to distribute to admin
    private short adminPlayerNumericCode; // by default 1

    //private List<List<CardPOJO>> setsWonList; do not include for now
    private short setsWon;
}
