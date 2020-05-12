package com.cardsGame.model;

import com.cardsGame.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity // This tells Hibernate to make a table out of this class
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Move {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String gameCode;

    private Short card;

    private String playerCode;

    private Boolean isActive;

    public static int compare(Move o1, Move o2) {
        short greaterCard = CommonUtil.getGreaterCard(o1.card, o2.card);
        if (o1.getCard().equals(greaterCard) && o2.getCard().equals(greaterCard)) {
            return 0;
        }
        if (o1.getCard().equals(greaterCard)) {
            return 1;
        }
        return -1;
    }
}
