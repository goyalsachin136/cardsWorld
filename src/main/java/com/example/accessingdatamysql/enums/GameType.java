package com.example.accessingdatamysql.enums;

import com.example.accessingdatamysql.util.CommonUtil;

public enum  GameType {
    FIVE_ZERO_EIGHT, CHOGDI;

    public static Integer getPointForFIVE_ZERO_EIGHT(Short card) {
        short index = CommonUtil.getCardIndex(card);
        if ((short) (index + 1) == (short)12) {
            return 50;
        }
        return index+1;
    }
}
