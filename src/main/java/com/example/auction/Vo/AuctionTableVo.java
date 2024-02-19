package com.example.auction.Vo;

import lombok.Data;

@Data
public class AuctionTableVo {
    private String no;
    private String image;
    private String contentName;
    private String seller;
    private String startTime;
    private String startPrice;
    private String nowPrice;
}
