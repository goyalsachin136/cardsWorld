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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity // This tells Hibernate to make a table out of this class
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardSet {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String gameCode;

    private String moveIds; // comma separated

    private Boolean isCurrentSet;

    private String winnerPlayerCode;

    public List<Long> getAllMoveIds() {
        if (null == moveIds) {
            return Collections.emptyList();
        }
        return Arrays.asList(moveIds.split(",")).stream().map(x -> new Long(x)).collect(Collectors.toList());
    }

    public void addMoveId(Long moveId) {
        if (null == moveIds) {
            moveIds = String.valueOf(moveId);
        } else {
            moveIds = String.format("%s,%s", moveIds, moveId);
        }
    }
}
