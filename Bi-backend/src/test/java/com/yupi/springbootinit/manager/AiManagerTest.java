package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiManagerTest {
    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String answer = aiManager. doChat(1659171950288818178L, "分析需求: 分析网站用户的增长情况\n原始数据: 日期,用户数\n1号,10\n2号,12\n3号,15\n4号,8\n");
        System.out.println(answer);
    }
}