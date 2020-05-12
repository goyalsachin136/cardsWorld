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
public class CardSetDTO {
    private String displayCard;
    private Short playerNumericCode;
    private CardType cardType;
    private String playerNickName;
}
