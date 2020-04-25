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
public class CardSetDTO {
    private String displayCard;
    private short playerNumericCode;
    private CardType cardType;
}
