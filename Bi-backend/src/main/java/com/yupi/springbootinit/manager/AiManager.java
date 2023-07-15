package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName AiManager
 * @Description TODO
 * @Author Administrator
 * @Date 2023/7/15 12:36
 * @Version 1.0
 */
@Service
public class AiManager {
    @Resource
    private YuCongMingClient client;

    /**
     * Ai对话 AIId:1659171950288818178L
     * @param message
     * @return
     */
    public String doChat(Long mdoelId, String message){
        // 构造请求
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(mdoelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if(response == null){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR, "Ai响应异常");
        }
        return response.getData().getContent();
    }

}
