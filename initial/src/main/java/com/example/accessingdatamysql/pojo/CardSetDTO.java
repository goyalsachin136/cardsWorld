package com.example.accessingdatamysql.pojo;

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
}
