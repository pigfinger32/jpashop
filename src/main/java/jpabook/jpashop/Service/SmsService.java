package jpabook.jpashop.Service;

import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class SmsService {

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Value("${coolsms.from}")
    private String fromNumber;

    @Value("${coolsms.admin-phone}")
    private String adminPhone;

    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    /** 예약자에게 신청 완료 + 입금 안내 */
    public void sendOrderConfirmation(String toPhone, String orderName, String startDate, String endDate,
                                      int totalAmount, String payDeadline) {
        if (!StringUtils.hasText(toPhone)) return;
        send(toPhone,
            "[여수가로기] '" + orderName + "' 게첨 신청이 완료되었습니다.\n" +
            "기간: " + startDate + " ~ " + endDate + "\n" +
            "문의: 010-8744-0026\n" +
            "아래 계좌로 " + payDeadline + "까지 " + String.format("%,d", totalAmount) + "원 입금해주세요.\n" +
            "기업(193-110190-04-013) 주식회사아이비");
    }

    /** 담당자에게 새 예약 입금 확인 요청 */
    public void sendAdminNotification(String orderName, String company, String startDate, String endDate,
                                      int totalCount) {
        send(adminPhone,
            "[여수가로기] 새 예약\n" +
            "업체: " + company + "\n" +
            "공연명: " + orderName + "\n" +
            "기간: " + startDate + " ~ " + endDate + "\n" +
            "수량: " + totalCount + "개\n" +
            "입금을 2일 안으로 확인하시고 입금완료로 변경해주세요.");
    }

    private void send(String toPhone, String text) {
        try {
            Message message = new Message();
            message.setFrom(fromNumber.replaceAll("-", ""));
            message.setTo(toPhone.replaceAll("-", ""));
            message.setText(text);
            messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("SMS 발송 완료: {}", toPhone);
        } catch (Exception e) {
            log.warn("SMS 발송 실패 ({}): {}", toPhone, e.getMessage());
        }
    }
}
