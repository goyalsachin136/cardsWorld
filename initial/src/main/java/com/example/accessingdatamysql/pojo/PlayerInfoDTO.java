package com.example.accessingdatamysql.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerInfoDTO {
    private short numericCode;
    private Integer setsWon;
    private short cardsLeft;
    private Integer points;
}
