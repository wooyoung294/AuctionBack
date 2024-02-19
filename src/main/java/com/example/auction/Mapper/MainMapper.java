package com.example.auction.Mapper;

import com.example.auction.Vo.AuctionListItemVo;
import com.example.auction.Vo.AuctionOpenNo;
import com.example.auction.Vo.AuctionTableVo;
import com.example.auction.Vo.UserVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface MainMapper {
    UserVo findByUser(UserVo userVo);

    int createUser(UserVo userVo);

    int duplicateIdChecked(String id);

    int updateToken(Map<String, Object> params);

    int createSellItem(AuctionListItemVo auctionListItem);

    List<AuctionTableVo> getListData();

    String getNextNo();

    AuctionListItemVo getItem(String no);

    int checkedMoney(String id);

    List<AuctionOpenNo> openAuction(String formattedTime);

    int setPriceAndBuyer(String id, int money, int no);

    int updateAuctionStatus(int no, String status);

    AuctionListItemVo selectAuctionByNo(int no);

    int setAuctionResult(String id, int price);

    int userAmount(String id);
}
