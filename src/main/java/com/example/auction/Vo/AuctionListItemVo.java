package com.example.auction.Vo;

import lombok.Data;

@Data
public class AuctionListItemVo {
    private String no;
    private String imgPath1;
    private String imgPath2;
    private String imgPath3;
    private String contentName;
    private String seller;
    private String startTime;
    private String startPrice;
    private String nowPrice;
    private String buyerId;
    private String status;
}
