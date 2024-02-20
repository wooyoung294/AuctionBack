package com.example.auction.Service;

import com.example.auction.Vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MainServiceIF {
    UserVo findBy(UserVo userVo);

    int createUser(UserVo userVo);

    int duplicateIdChecked(String id);

    int updateToken(String id, String token);

    int createSellItem(String contentName, String amount, String seller, MultipartFile[] filePath, String startTime);

    List<AuctionTableVo> getListData();

    AuctionListItemVo getItem(String no) throws Exception;

    int checkedMoney(String id, int money, int no);

    List<AuctionOpenNo> openAuction(String formattedTime);


    int setAuctionResult(String no);

    int userAmount(String id);

    TokenVo tokenUpdate(TokenVo tokenVo);
}
