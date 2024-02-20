package com.example.auction.Service;

import com.example.auction.Mapper.MainMapper;
import com.example.auction.Provider.JwtTokenProvider;
import com.example.auction.Vo.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MainService implements MainServiceIF{
    private List<AuctionOpenNo> no = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    private String projectRoot = System.getProperty("user.dir");
    private String uploadDir = projectRoot + "/uploads/";
    private final MainMapper mainMapper;
    private final JwtTokenProvider jwtTokenProvider;
    public MainService(MainMapper mainMapper,JwtTokenProvider jwtTokenProvider) {
        this.mainMapper = mainMapper;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public UserVo findBy(UserVo userVo) {
        return mainMapper.findByUser(userVo);
    }

    @Override
    public int createUser(UserVo userVo) {
        int result = 0;
        result =  mainMapper.createUser(userVo);
        if(result ==1){
            return 1;
        }
        else{
            return 0;
        }
    }

    @Override
    public int duplicateIdChecked(String id) {
        return mainMapper.duplicateIdChecked(id);
    }

    @Override
    public int updateToken(String id, String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("token", token);
        return mainMapper.updateToken(params);
    }

    @Override
    public int createSellItem(String contentName, String amount, String seller,MultipartFile[] files, String startTime) {
        try {
            StringBuilder fileNames = new StringBuilder();  // 파일 이름을 저장할 StringBuilder

            // 파일이 존재하고, 배열을 순회하면서 각 파일 처리
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];

                    // 파일의 원본 이름 얻기
                    String originalFileName = file.getOriginalFilename();

                    String no =mainMapper.getNextNo();

                    // 업로드 디렉토리에 파일 저장
                    String fullPath = uploadDir+ "/"+ no + "/" +originalFileName;
                    File dir = new File(uploadDir+ "/"+ no);

                    if (!dir.exists()) {
                        dir.mkdirs();  // 디렉토리가 없으면 생성
                    }

                    file.transferTo(new File(fullPath));

                    // 파일 이름을 StringBuilder에 추가
                    if (fileNames.length() > 0) {
                        fileNames.append(", ");
                    }
                    fileNames.append(originalFileName);
                }
            }
            AuctionListItemVo auctionListItem = new AuctionListItemVo();
            auctionListItem.setContentName(contentName);
            auctionListItem.setStartPrice(amount);
            auctionListItem.setSeller(seller);
            auctionListItem.setStartTime(startTime);

            String[] filePaths = fileNames.toString().split(", ");
            if (filePaths.length >= 1) {
                auctionListItem.setImgPath1(filePaths[0]);
            }
            if (filePaths.length >= 2) {
                auctionListItem.setImgPath2(filePaths[1]);
            }
            if (filePaths.length >= 3) {
                auctionListItem.setImgPath3(filePaths[2]);
            }

            // 파일 업로드 성공 시 파일 이름들을 응답
            int result =mainMapper.createSellItem(auctionListItem);
            return  result;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<AuctionTableVo> getListData() {
        List<AuctionTableVo> auctionList = mainMapper.getListData();
        try {
            for (AuctionTableVo auction : auctionList) {
                String base64Image = encodeImage(uploadDir+ "/"+ auction.getNo() + "/"+auction.getImage());

                // AuctionTableVo의 img 필드에 Base64 이미지 문자열 설정
                auction.setImage(base64Image);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return auctionList;
    }

    @Override
    public AuctionListItemVo getItem(String no) throws Exception {
        AuctionListItemVo auctionListItemVo = mainMapper.getItem(no);
        try {
            String base64Image1 = encodeImage(uploadDir+ "/"+ auctionListItemVo.getNo() + "/"+auctionListItemVo.getImgPath1());
            auctionListItemVo.setImgPath1(base64Image1);
            if(auctionListItemVo.getImgPath2() != null){
                String base64Image2 = encodeImage(uploadDir+ "/"+ auctionListItemVo.getNo() + "/"+auctionListItemVo.getImgPath2());
                auctionListItemVo.setImgPath2(base64Image2);
            }
            if(auctionListItemVo.getImgPath3() != null){
                String base64Image3 = encodeImage(uploadDir + "/" + auctionListItemVo.getNo() + "/" + auctionListItemVo.getImgPath3());
                auctionListItemVo.setImgPath3(base64Image3);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }
        return auctionListItemVo;
    }

    @Override
    public int checkedMoney(String id, int money, int no) {
         int result = mainMapper.checkedMoney(id);
         if(money < result ){

            return mainMapper.setPriceAndBuyer(id,money,no);
         }
         else{
             return 0;
         }
    }

    @Override
    public List<AuctionOpenNo> openAuction(String formattedTime) {

        List<AuctionOpenNo> auctionOpenNoList = mainMapper.openAuction(formattedTime);
        return auctionOpenNoList;
    }


    @Override
    public int setAuctionResult(String no) {
        mainMapper.updateAuctionStatus(Integer.parseInt(no),"finish");
        AuctionListItemVo auctionListItemVo = mainMapper.selectAuctionByNo(Integer.parseInt(no));
        if(auctionListItemVo==null){
            mainMapper.setAuctionResult(auctionListItemVo.getBuyerId(),Integer.parseInt(auctionListItemVo.getNowPrice()));
        }
        return 1;
    }

    @Override
    public int userAmount(String id) {
        return mainMapper.userAmount(id);
    }

    @Override
    public TokenVo tokenUpdate(TokenVo tokenVo) {
        TokenVo prevTokenVo = mainMapper.findTokenByUser(tokenVo.getId());
        Timestamp expireDateTime = prevTokenVo.getExpireTime();
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        TokenVo newTokenVo = new TokenVo();
//        if (expireDateTime.before(currentTimestamp) && tokenVo.getToken().equals(prevTokenVo.getToken())) {
        if (currentTimestamp.before(expireDateTime) && tokenVo.getToken().equals(prevTokenVo.getToken())) {
            String token = jwtTokenProvider.createToken(tokenVo.getId());
            Map<String, Object> params = new HashMap<>();
            params.put("id", tokenVo.getId());
            params.put("token", token);
            int updateResult = mainMapper.updateToken(params);
            if(updateResult == 1){
                newTokenVo = mainMapper.findTokenByUser(tokenVo.getId());
            }
            else{
                newTokenVo.setId("SYSTEM");
                newTokenVo.setToken("EXPIRE");
            }
        } else {
            newTokenVo.setId("SYSTEM");
            newTokenVo.setToken("EXPIRE");
        }
        return newTokenVo;
    }

    private static String encodeImage(String imagePath) throws Exception {
        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
