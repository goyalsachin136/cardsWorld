package com.example.accessingdatamysql.pojo;

import com.example.accessingdatamysql.enums.CardType;
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
    private CardType cardType;
    private String displayCode;
}
