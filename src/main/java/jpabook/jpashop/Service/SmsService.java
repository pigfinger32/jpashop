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

    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    public void sendOrderConfirmation(String toPhone, String orderName, String startDate, String endDate) {
        if (!StringUtils.hasText(toPhone)) return;
        try {
            Message message = new Message();
            message.setFrom(fromNumber.replaceAll("-", ""));
            message.setTo(toPhone.replaceAll("-", ""));
            message.setText("[여수가로기] '" + orderName + "' 게첨 신청 완료\n기간: " + startDate + " ~ " + endDate + "\n문의: 061-652-2190");
            messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("SMS 발송 완료: {}", toPhone);
        } catch (Exception e) {
            log.warn("SMS 발송 실패 ({}): {}", toPhone, e.getMessage());
        }
    }
}
