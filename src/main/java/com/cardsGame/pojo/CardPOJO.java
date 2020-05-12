package com.cardsGame.pojo;

import com.cardsGame.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardPOJO {
    private short card;
    private short cardNumber;
    private CardType cardType;
    private String displayCode;
}
