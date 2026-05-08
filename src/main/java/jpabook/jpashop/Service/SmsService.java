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

    /** 예약자에게 신청 완료 안내 */
    public void sendOrderConfirmation(String toPhone, String orderName, String startDate, String endDate) {
        if (!StringUtils.hasText(toPhone)) return;
        send(toPhone, "[여수가로기] '" + orderName + "' 게첨 신청이 완료되었습니다.\n기간: " + startDate + " ~ " + endDate + "\n문의: 010-8744-0026");
    }

    /** 담당자에게 새 예약 입금 확인 요청 */
    public void sendAdminNotification(String orderName, String memberName, String startDate, String endDate) {
        send(adminPhone, "[여수가로기] 새 예약\n업체: " + memberName + "\n공연명: " + orderName + "\n기간: " + startDate + " ~ " + endDate + "\n입금을 2일 안으로 확인해 주세요.");
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
