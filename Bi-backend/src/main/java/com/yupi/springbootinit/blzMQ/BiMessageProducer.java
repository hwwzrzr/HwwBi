package com.yupi.springbootinit.blzMQ;

import com.yupi.springbootinit.constant.RabbitMQConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName BiPublicer
 * @Description TODO
 * @Author Administrator
 * @Date 2023/7/19 14:47
 * @Version 1.0
 */
@Component
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(RabbitMQConstant.BI_EXCHANGE,RabbitMQConstant.ROUTINGKEY, message);
    }
}
