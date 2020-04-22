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

    // card number will be from 0 to 51 inclusive
    // HEARTS(0), DIAMOND(1), SPADE(2), CLUBS(3) --> bracket number represent card number / 13
    public static String getDisplayStringForCard(short card) {
        short index = (short) (card/13);
        CardType cardType = CardType.getFromIndex(index);
        int cardNumber = index % 13;

        if (cardNumber == 0) {
            return String.format("%s-%s", "A", cardType.name());
        }
        if (cardNumber + 1 == 11) {
            return String.format("%s-%s", "J", cardType.name());
        }
        if (cardNumber + 1 == 12) {
            return String.format("%s-%s", "Q", cardType.name());
        }
        if (cardNumber + 1 == 13) {
            return String.format("%s-%s", "K", cardType.name());
        }
        return String.format("%s-%s", cardNumber, cardType.name());
    }

}
