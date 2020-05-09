package com.example.accessingdatamysql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity // This tells Hibernate to make a table out of this class
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Player {


    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id; // numeric

    private Short numericCode;

    private String gameCode;

    private String code;

    private String cardsLeft; // comma separated numbers

    private String nickName;

    //TODO leave it for now as there is winnerPlayerCode in card set
    //private String setWonList; // comma separated sets ids ,

    public List<Short> getAllCards() {
        if (null == cardsLeft) {
            return Collections.emptyList();
        }
        return Arrays.asList(cardsLeft.split(",")).stream().map(x -> new Short(x)).collect(Collectors.toList());
    }

    public void setCardsLeftFromList(List<Short> cardsLeftFromList) {
        if (cardsLeftFromList.isEmpty()) {
            this.cardsLeft = null;
        } else {
            this.cardsLeft = null;
            for (int i =0; i < cardsLeftFromList.size(); i++) {
                this.addCard(cardsLeftFromList.get(i));
            }
        }
    }

    public void addCard(short card) {
        if (null == cardsLeft) {
            cardsLeft = String.valueOf(card);
        } else {
            cardsLeft = String.format("%s,%s", cardsLeft, card);
        }
    }
}
