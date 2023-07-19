package com.yupi.springbootinit.blzMQ;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName BiDeclare
 * @Description TODO
 * @Author Administrator
 * @Date 2023/7/19 14:35
 * @Version 1.0
 */
@Configuration
public class BiDeclare {
    public static final String BI_EXCHANGE = "biExchange";
    public static final String BI_Queue = "biQueue";
    //声明交换机
    @Bean("biExchange")
    public DirectExchange biExchange(){
        return new DirectExchange(BI_EXCHANGE);
    }
    //声明队列
    @Bean("biQueue")
    public Queue biQueue(){
        return QueueBuilder.durable(BI_Queue).build();
    }

    //将队列绑定到交换机上
    @Bean
    public Binding queueBindingExchange(@Qualifier("biExchange") DirectExchange biExchange,
                                        @Qualifier("biQueue") Queue biQueue){
        return BindingBuilder.bind(biQueue).to(biExchange).with("BiMessage");

    }

}
