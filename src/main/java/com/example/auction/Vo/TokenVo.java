package com.example.auction.Vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class TokenVo {
    private String id;
    private String token;
    private Timestamp expireTime;
}
