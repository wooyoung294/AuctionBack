package com.example.auction.Controller;

import com.example.auction.Provider.JwtTokenProvider;
import com.example.auction.Service.MainService;
import com.example.auction.Vo.*;
import io.micrometer.core.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@EnableScheduling
public class MainController {
    private final MainService mainService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SimpMessagingTemplate simpMessagingTemplate;
//    private final TimerService timerService;
    @Autowired
    public MainController(JwtTokenProvider jwtTokenProvider, MainService mainService, SimpMessagingTemplate simpMessagingTemplate
                          ) {
        this.mainService = mainService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.simpMessagingTemplate = simpMessagingTemplate;
//        this.timerService = timerService;
    }

    @PostMapping("/login")
    public UserVo login(@RequestBody final UserVo params) {
        UserVo userVo = mainService.findBy(params);

        if (userVo != null) {
            // 사용자가 존재하면 토큰 생성 및 업데이트
            String token = jwtTokenProvider.createToken(userVo.getId());
            userVo.setToken(token);
            mainService.updateToken(userVo.getId(), token);
        }
        else{
            System.out.println("null");
        }
        return userVo;
    }
    @PostMapping("/tokenUpdate")
    public TokenVo tokenUpdate(@RequestBody TokenVo tokenVo){

        return mainService.tokenUpdate(tokenVo);

    }
    @GetMapping("/duplicateId")
    public int duplicateIdChecked(@RequestParam(value = "id",required = true) String id){
        return mainService.duplicateIdChecked(id);
    }

    @RequestMapping(value = "/createUser",method = {RequestMethod.POST,RequestMethod.OPTIONS})
    @ResponseBody
    public int createUser(@RequestBody final UserVo params) {
        return mainService.createUser(params);
    }

    @PostMapping("/createSellItem")
    public int handleFileUpload(
            @RequestParam("contentName") String contentName,
            @RequestParam("amount") String amount,
            @RequestParam("seller") String seller,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam("startTime") String startTime
    ) {
        int result = mainService.createSellItem(contentName, amount, seller,files, startTime);
        return result;
    }
    @GetMapping("/getItem")
    public AuctionListItemVo getItem(@RequestParam(value = "no",required = true) String no) throws Exception {

        return mainService.getItem(no);
    }

    @GetMapping("/getListData")
    public List<AuctionTableVo> getListData() {
        return mainService.getListData();
    }

    @GetMapping("/checkedMoney")
    public int checkedMoney(@RequestParam("id") String id,
                            @RequestParam("money") int money,
                            @RequestParam("no") int no
    ) {
        return mainService.checkedMoney(id,money,no);
    }
    @GetMapping("/chargeMoney")
    public int chargeMoney(@RequestParam("id") String id, @RequestParam("money") int money){
        return mainService.chargeMoney(id,money);
    }
    @GetMapping("/userAmount")
    public int userAmount( @RequestParam("id") String id) {
        return mainService.userAmount(id);
    }@GetMapping("/updateTutorial")
    public int updateTutorial( @RequestParam("id") String id) {
        return mainService.updateTutorial(id);
    }

    @MessageMapping("/chat")
    public void sendMessage(Message message, SimpMessageHeaderAccessor accessor, Principal principal) {
        // 개인적인 환영 메시지 보내기
//        if(lastMessage!=null && !lastMessage.getWriterId().equals("SYSTEM") && message.getWriterId().equals("SYSTEM")){
////            simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/queue/welcome/"+ message.getChannelId(), lastMessage);
//            simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/sub/chat/" + message.getChannelId(), lastMessage);
//        }
//        else{
//            message.setTimeLeft(300);
//            simpMessagingTemplate.convertAndSend("/sub/chat/" + message.getChannelId(), message);
//            if(!message.getWriterId().equals("SYSTEM")){
//                lastMessage=message;
//            }
//        }
        simpMessagingTemplate.convertAndSend("/sub/chat/" + message.getChannelId(), message);
    }

    private int sec = 299;
    public void setSec(int newSec) {
        this.sec = newSec;
    }
    private List<AuctionOpenNo> no = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    @Scheduled(fixedRate = 1000) // 1초마다 실행
    public void sendTimerUpdates() {
//        System.out.println("sendTimerUpdates ");
        if (no.size()!=0) {
            for (AuctionOpenNo auction : no) {
                handleIndividualItem(auction);
            }
            sec--;
        }
    }

    private void handleIndividualItem(AuctionOpenNo auction) {
        String noValue = auction.getNo();
        String secStr = Integer.toString(sec);
        String messageContent = "{\"channelId\":\"" + noValue + "\", \"writerId\":\"timer\", \"chat\":\"" + secStr + "\"}";
        simpMessagingTemplate.convertAndSend("/sub/chat/" + noValue, messageContent);

        if (sec <= 0) {
            String closeMessageContent = "{\"channelId\":\""+noValue+"\", \"writerId\":\"timer\", \"chat\":\"close\"}";
            simpMessagingTemplate.convertAndSend("/sub/chat/" + noValue, closeMessageContent);
        }
    }
    @Async
//    @Scheduled(cron = "0 * * * * *") // 매시간 0분 0초에 실행
    @Scheduled(cron = "0 0/5 * * * *")
    public void scheduledMethod() {
        setSec(299);
        for (int i = 0; i < no.size(); i++) {
            mainService.setAuctionResult(no.get(i).getNo());
        }
        no = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = currentTime.format(formatter);

        List<AuctionOpenNo> result = mainService.openAuction(formattedTime);
        if(!result.isEmpty()){
            no = new ArrayList<>(result);
        }
    }
}
