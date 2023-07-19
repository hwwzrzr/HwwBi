package com.yupi.springbootinit.blzMQ;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.RabbitMQConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName BiConsumer
 * @Description TODO
 * @Author Administrator
 * @Date 2023/7/19 14:47
 * @Version 1.0
 */
@Slf4j
@Component
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {RabbitMQConstant.BI_Queue}, ackMode = "MANUAL")
    public void receive(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        if(StringUtils.isBlank(message)){
            //失败了 拒绝消息
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart == null){
            //数据库中没有这个用户请求，拒绝消息
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表为空");
        }
        // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
        Chart chartUpdate = new Chart();
        chartUpdate.setId(chartId);
        chartUpdate.setStatus("running");
        boolean saveTask = chartService.updateById(chartUpdate);
        if(!saveTask){
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chartId, "更新图表执行中状态失败");
            return;
        }
        /**
         * 将用户输入的goal，chartType、chartName、上传的文件等构造成用户输入
         * 处理用户数据、构建用户请求
         */
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csv = chart.getChartData();

        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求: ").append("\n");
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal = goal + ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据: ").append("\n");
        //读取用户上传de excel文件,需要进行一个处理
        userInput.append(csv).append("\n");

        //调用鱼聪明sdk，得到AI响应结果
        String aiAnswer = aiManager.doChat(CommonConstant.BI_MODEL_ID, userInput.toString());

        //从AI响应结果中取出需要的信息
        String[] split = aiAnswer.split("【【【【【");
        if(split.length<3){
            channel.basicNack(deliveryTag, false, true);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Ai生成错误");
        }
        String genChart = split[1].trim();
        String genResult = split[2].trim();
        channel.basicAck(deliveryTag,false);
        //调用Ai得到结果，再更新一次
        chartUpdate.setStatus("succeed");
        chartUpdate.setGenChart(genChart);
        chartUpdate.setGenResult(genResult);
        boolean b = chartService.updateById(chartUpdate);
        if(!b){
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
    }

    //上面接口很多用到异常，直接定义一个工具类
    private void handleChartUpdateError(long chartId, String execMessage){
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setExecMessage(execMessage);
        updateChartResult.setStatus("failed");
        boolean b = chartService.updateById(updateChartResult);
        if(!b){
            log.error("更新图表失败状态失败"+chartId+"," + execMessage);
        }
    }
}
