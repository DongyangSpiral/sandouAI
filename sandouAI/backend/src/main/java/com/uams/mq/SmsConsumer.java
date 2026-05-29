package com.uams.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SmsConsumer {

    @RabbitListener(queues = "sms.queue")
    public void handleSms(Map<String, String> msg) {
        String phone = msg.get("phone");
        String code = msg.get("code");
        log.info("发送短信验证码到 {}: {}", phone, code);
    }
}
