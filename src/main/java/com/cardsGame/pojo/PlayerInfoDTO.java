package com.cardsGame.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerInfoDTO {
    private Short numericCode;
    private Integer setsWon;
    private short cardsLeft;
    private Integer points;
    private String nickName;
    private boolean toMove;
}
