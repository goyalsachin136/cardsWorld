package com.example.accessingdatamysql.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum  CardType {
    HEARTS((short) 0), DIAMOND((short) 1), SPADE((short) 2), CLUBS((short) 3);
    private short cardIndex;

    public static CardType getFromIndex(short index) {
        return Arrays.stream(CardType.values()).filter(cardType -> index == cardType.getCardIndex()).findFirst().orElse(null);
    }
}
