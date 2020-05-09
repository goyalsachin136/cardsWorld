package com.example.accessingdatamysql.util;

import com.example.accessingdatamysql.enums.CardType;

import java.util.Random;

public class CommonUtil {

    public static String getSmallCapRandomString(Short length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
    }

    //assuming both are of same type
    public static short getGreaterCard(short card1, short card2) {
        short index1 = getCardIndex(card1);
        short index2 = getCardIndex(card2);
        if (index1 == 0) {
            return card1;
        }
        if (index2 == 0) {
            return card2;
        }
        return index1 > index2 ? card1 : card2;
    }
    public static CardType getCardType(short card) {
        short index = (short) (card/13);
        return CardType.getFromIndex(index);
    }

    public static short getCardIndex(short card) {
        return  (short) (card % 13);
    }

    // card number will be from 0 to 51 inclusive
    // HEARTS(0), DIAMOND(1), SPADE(2), CLUBS(3) --> bracket number represent card number / 13
    public static String getDisplayStringForCard(short card) {
        CardType cardType = getCardType(card);
        if (cardType == null) {
            return "";
        }
        short cardIndex = getCardIndex(card);
        if (cardIndex == 0) {
            return String.format("%s", "A");
        }
        if (cardIndex + 1 == 11) {
            return String.format("%s", "J");
        }
        if (cardIndex + 1 == 12) {
            return String.format("%s", "Q");
        }
        if (cardIndex + 1 == 13) {
            return String.format("%s", "K");
        }
        return String.format("%s",cardIndex+1)   ;
        /*short cardIndex = getCardIndex(card);
        if (cardIndex == 0) {
            return String.format("%s-%s", "A", cardType.name());
        }
        if (cardIndex + 1 == 11) {
            return String.format("%s-%s", "J", cardType.name());
        }
        if (cardIndex + 1 == 12) {
            return String.format("%s-%s", "Q", cardType.name());
        }
        if (cardIndex + 1 == 13) {
            return String.format("%s-%s", "K", cardType.name());
        }
        return String.format("%s-%s", cardIndex, cardType.name());*/
    }

}
