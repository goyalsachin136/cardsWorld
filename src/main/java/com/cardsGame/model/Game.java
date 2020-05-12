package com.cardsGame.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String code; // 6 digit all small alphabet

    private Short currentPlayer; // Player who has to move -- numeric code

    private Short trumpCard; // HEARTS(0), DIAMOND(1), SPADE(2), CLUBS(3) --> bracket number represent card number / 13

    private String trumpSetByPlayerCode;

    private Boolean isTrumpOpen;

    private Short numberOfPlayers;

    private Short numberOfCards; // 0 to 51

    private Boolean canGameBeStarted;

    private String gameType;

    public short getNextPlayerToMove() {
        if (null == currentPlayer) {
            throw new RuntimeException("First decide winner of current game set");
        }
        if (currentPlayer.equals(numberOfPlayers)) {
            return (short)1;
        }
        return (short)(currentPlayer + 1);
    }

}
