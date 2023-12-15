package com.hty.service.ai.impl;

import com.alibaba.fastjson.JSON;
import com.hty.constant.ChatModel;
import com.hty.dao.ai.OpenaiChatHistoryMessageMapper;
import com.hty.dao.ai.OpenaiChatWindowMapper;
import com.hty.entity.ai.ChatRequestParam;
import com.hty.entity.ai.ChatResponseBody;
import com.hty.entity.ai.StreamChatResponseBody;
import com.hty.entity.ai.Usage;
import com.hty.entity.pojo.OpenaiChatHistoryMessage;
import com.hty.entity.pojo.OpenaiChatWindow;
import com.hty.service.ai.ChatService;
import com.hty.utils.ai.ChatUtil;
import com.hty.utils.SSEUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author hty
 * @date 2023-12-07 13:24
 * @email 1156388927@qq.com
 * @description
 */

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatUtil chatUtil;
    @Resource
    private SSEUtils sseUtils;
    @Resource
    private OpenaiChatWindowMapper openaiChatWindowMapper;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private OpenaiChatHistoryMessageMapper openaiChatHistoryMessageMapper;

    //TODO:这里消息需要放在redis中，使用list数据结构，key是窗口id,每个value都是一个Map
    //历史对话，需要按照user,assistant的顺序排列 使用队列方便控制上下文长度
    LinkedList<Map<String, String>> messages = new LinkedList<>();

    //用来异步发送消息
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public String chat(String question, String windowId) {
        if (question == null || question.equals("")) {
            log.info("用户请求过来的问题为空");
            return null;
        }

        //获取消息列表
        LinkedList<Map<String, String>> messages = getMessageList(windowId);
        //判断消息列表是否成功被获取
        if (messages == null) {
            return null;
        }
        //添加本次问题
        chatUtil.addUserQuestion(question, messages);

        //设置请求的参数信息(聊天的配置信息)
        ChatRequestParam requestParam = new ChatRequestParam();
        requestParam.setMessages(messages);
        requestParam.setModel(ChatModel.GPT_3_5_TURBO);

        String responseJSON;
        String answer = null;
        try(
                //请求AI
                Response response = chatUtil.chat(requestParam);
                ResponseBody responseBody = response.body()
        ) {

            if(responseBody != null){
                responseJSON = responseBody.string();
                //解析JSON字符串
                ChatResponseBody chatResponseBody = JSON.parseObject(responseJSON, ChatResponseBody.class);

                //统计token消耗
                Usage usage = new Usage(chatResponseBody.getUsage().getPromptTokens(),
                        chatResponseBody.getUsage().getCompletionTokens(),
                        chatResponseBody.getUsage().getTotalTokens());
                log.info("本次请求输入消耗{}tokens,输出消耗{}tokens,总计消耗{}tokens",usage.getPromptTokens(),usage.getCompletionTokens(),usage.getTotalTokens() );

                answer = chatResponseBody.getChoices()[0].getMessage().getContent();
                //存储到redis中
                putMessage2Redis(question, answer, windowId);
                //存储到mysql中
                //向mysql中添加数据
                putMessage2Mysql(usage, "user", question, windowId);
                putMessage2Mysql(usage, "assistant", answer, windowId);
            }else{
                log.info("AI返回的回答为空");
            }
        } catch (IOException e) {
            log.error("请求发起失败 => {}",e.getMessage());
        }

        return answer;
    }

    //TODO:当前方法的参数中应该添加一个windowId,方便修改聊天的prompt
    @Override
    public void setPrompt(String prompt) {

        Map<String, String> systemPrompt = new HashMap<>();
        systemPrompt.put("role", "system");
        systemPrompt.put("content", prompt);
        messages.addFirst(systemPrompt);
        log.info("设置prompt => {}", prompt);
    }

    @Override
    public void streamChat(String question, Long clientId, String windowId) {
        if (question == null || question.equals("")) {
            log.info("用户请求过来的问题为空");
            return;
        }

        //这里判断数据库中是否存在这个窗口
        if (openaiChatHistoryMessageMapper.countWindowMessage(windowId) == 0) {
            log.info("窗口{}不存在或窗口有问题，没有设置prompt提示词，请重新创建窗口", windowId);
            return;
        }

        //获取消息列表
        LinkedList<Map<String, String>> messages = getMessageList(windowId);

        //判断消息列表是否成功被获取
        if (messages == null) {
            return;
        }

        //异步发送消息
        executorService.execute(() -> {

            //将当前问题插入到消息列表中
            chatUtil.addUserQuestion(question, messages);

            //设置请求的参数信息(聊天的配置信息)
            ChatRequestParam requestParam = new ChatRequestParam();
            requestParam.setMessages(messages);
            requestParam.setModel(ChatModel.GPT_3_5_TURBO);
            requestParam.setStream(true);

            // 发起异步请求
            Response response = chatUtil.chat(requestParam);
            if (response == null) {
                return;
            }

            BufferedReader reader = null;
            ResponseBody responseBody = null;
            // 发起异步请求
            try {
                responseBody = response.body();
                if (responseBody == null) {
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                String str;
                //最终的回答
                StringBuilder answer = new StringBuilder();
                //因为一条消息内容太少，所以进行消息合并然后发送
                StringBuilder sseBuffer = new StringBuilder();
                while ((str = reader.readLine()) != null) {
                    //解析每条数据 最终结束标志为 data: [DONE]
                    if (!str.equals("data: [DONE]")) {
                        //前面有data前缀
                        StreamChatResponseBody streamChatResponseBody = JSON.parseObject(str.substring(6), StreamChatResponseBody.class);
                        String content = streamChatResponseBody.getChoices()[0].getDelta().getContent();
                        if (content != null) {
                            //增加一个缓冲区,因为ChatGPT流式回答中每条内容太少，会产生过多的请求
                            if (sseBuffer.length() > 20) {
                                if (!sseUtils.sendMessage(clientId, sseBuffer.toString())) {
                                    log.error("消息发送失败了，结束消息发送,失败的消息 => {}", sseBuffer);
                                    break;
                                }
                                sseBuffer = new StringBuilder();
                            }
                            answer.append(content);
                            sseBuffer.append(content);
                        }
                    }
                    //由于每条消息后面还有一个换行，需要将换行读取掉然后再继续读取下一条消息
                    reader.readLine();
                }
                //如果缓冲区还有没有发送的数据需要再次发送
                if (sseBuffer.length() != 0) {
                    if (!sseUtils.sendMessage(clientId, sseBuffer.toString())) {
                        log.error("消息发送失败了，结束消息发送,失败的消息 => {}", sseBuffer);
                    }
                }

                //统计token消耗
                Usage usage = chatUtil.computePromptToken(requestParam, answer.toString());
                log.info("本次请求输入消耗{}tokens,输出消耗{}tokens,总计消耗{}tokens",
                        usage.getPromptTokens(),
                        usage.getCompletionTokens(),
                        usage.getTotalTokens());

                //向redis中添加数据
                putMessage2Redis(question, answer.toString(), windowId);
                //向mysql中添加数据
                putMessage2Mysql(usage, "user", question, windowId);
                putMessage2Mysql(usage, "assistant", answer.toString(), windowId);

            } catch (IOException e) {
                log.error("流式请求出错,断开与{}的连接 => {}", clientId, e.getMessage());
                //移除当前的连接
                sseUtils.removeConnect(clientId);
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (responseBody != null)
                    responseBody.close();
                response.close();
            }
        });
    }

    /***
     * 向消息窗口新增数据
     * @param question
     * @param answer
     * @param windowId
     */
    public void putMessage2Redis(String question, String answer, String windowId) {
        //TODO:需要非空判断
        if (stringRedisTemplate.opsForList().size(windowId) == 0) {
            log.info("窗口{}不存在或窗口有问题，没有设置prompt提示词", windowId);
            return;
        }
        Map<String, String> input = new HashMap<>();
        input.put("role", "user");
        input.put("content", question);
        Map<String, String> output = new HashMap<>();
        output.put("role", "assistant");
        output.put("content", answer);
        stringRedisTemplate.opsForList().rightPush(windowId, JSON.toJSONString(input));
        stringRedisTemplate.opsForList().rightPush(windowId, JSON.toJSONString(output));
    }

    /***
     * 向mysql中插入数据
     * @param usage
     * @param role
     * @param content
     */
    public void putMessage2Mysql(Usage usage, String role, String content, String windowId) {
        //向mysql中插入前置提示词的消息
        OpenaiChatHistoryMessage newMessage = new OpenaiChatHistoryMessage(
                null,
                UUID.randomUUID().toString().replace("-", ""),
                role,
                content,
                new Date(),
                usage.getTotalTokens(),
                openaiChatWindowMapper.getIdByWindowId(windowId));
        Integer insertMessageRows = openaiChatHistoryMessageMapper.insertMessage(newMessage);
        if (insertMessageRows == 1) {
            log.info("消息成功存储 => {}", JSON.toJSONString(newMessage));
        } else {
            log.error("消息存储失败 => {}", JSON.toJSONString(newMessage));
        }
    }

    /***
     * 从mysql中读取消息列表并存储到redis中
     * @param windowId
     */
    public Boolean getMessageListFromMysqlSave2Redis(String windowId){
        //判断redis中是否存在，不存在就先从mysql中加载
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(windowId))) {
            List<OpenaiChatHistoryMessage> allMessages = openaiChatHistoryMessageMapper.getAllMessages(windowId);
            if (allMessages == null || allMessages.size() == 0) {
                log.info("窗口{}不存在", windowId);
                return false;
            }
            //加载进入redis中
            for (OpenaiChatHistoryMessage message : allMessages) {
                Map<String, String> map = new HashMap<>();
                map.put("role", message.getRole());
                map.put("content", message.getContent());
                stringRedisTemplate.opsForList().rightPush(windowId, JSON.toJSONString(map));
            }
        }

        return true;
    }

    /***
     * 获取消息列表且只获取不超过5对 10条消息
     * @param windowId
     * @return
     */
    public LinkedList<Map<String, String>> getMessageList(String windowId) {
        //从数据库中加载
        if(!getMessageListFromMysqlSave2Redis(windowId)) return null;

        //续费10分钟
        stringRedisTemplate.expire(windowId, 10, TimeUnit.MINUTES);
        LinkedList<Map<String, String>> messageList = new LinkedList<>();
        ListOperations<String, String> window = stringRedisTemplate.opsForList();
        //将prompt先添加进去
        messageList.add(JSON.parseObject(window.index(windowId, 0), Map.class));

        long size = window.size(windowId);
        //从末尾获取5对消息
        for (long i = (size - 1 > 10 ? size - 11 : 0) + 1; i < size; ++i) {
            messageList.add(JSON.parseObject(window.index(windowId, (int) i), Map.class));
        }
        return messageList;
    }

    @Transactional
    @Override
    public String createChatWindow(Integer userId, Integer modelId, String prompt) {
        log.info("正在创建聊天窗口");
        OpenaiChatWindow openaiChatWindow = new OpenaiChatWindow();
        openaiChatWindow.setWindowId(UUID.randomUUID().toString().replace("-", ""));
        openaiChatWindow.setModelId(modelId);
        openaiChatWindow.setUserId(userId);
        openaiChatWindow.setTitle("新聊天");
        openaiChatWindow.setCreateTime(new Date());

        //给数据库中插入这个窗口信息
        Integer rows = openaiChatWindowMapper.createWindow(openaiChatWindow);

        //在redis中开通窗口并同时设置前置提示词
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        if (prompt == null) prompt = "";
        system.put("content", prompt);
        Long rightPush = stringRedisTemplate.opsForList().rightPush(openaiChatWindow.getWindowId(), JSON.toJSONString(system));
        //key的过期时间设置为10分钟
        stringRedisTemplate.expire(openaiChatWindow.getWindowId(), 10, TimeUnit.MINUTES);

        if (rows == 1 && rightPush != null && rightPush == 1) {
            log.info("窗口创建成功,id => {}", openaiChatWindow.getWindowId());
        } else {
            log.error("窗口创建失败,mysql => {},redis => {}", rows, rightPush);
            throw new RuntimeException("窗口创建失败");
        }

        //向mysql中插入前置提示词的消息
        OpenaiChatHistoryMessage newMessage = new OpenaiChatHistoryMessage(
                null,
                UUID.randomUUID().toString().replace("-", ""),
                "system",
                prompt,
                new Date(),
                chatUtil.computeToken(prompt, ChatModel.GPT_3_5_TURBO),
                openaiChatWindowMapper.getIdByWindowId(openaiChatWindow.getWindowId()));
        Integer insertMessageRows = openaiChatHistoryMessageMapper.insertMessage(newMessage);
        if (insertMessageRows == 1) {
            log.info("消息成功存储 => {}", JSON.toJSONString(newMessage));
        } else {
            log.error("消息存储失败 => {}", JSON.toJSONString(newMessage));
        }

        return openaiChatWindow.getWindowId();
    }

    @Override
    public List<OpenaiChatHistoryMessage> getAllMessage(String windowId) {
        return openaiChatHistoryMessageMapper.getAllMessages(windowId);
    }

    @Transactional
    @Override
    public String generationTitle(String windowId,String question) {
        //判断是否被AI生成过
        if(openaiChatWindowMapper.getWindowTitleStatus(windowId) == 1){
            log.info("窗口{}的标题已经被生成过了，无法重新生成",windowId);
            return null;
        }
        //获取窗口的第一个问题

        //AI生成窗口标题
        question = "请为["+ question +"]这个问题生成一个标题,结果不要带引号,长度不能超过100个字符";
        ChatResponseBody responseBody = JSON.parseObject(chatUtil.chat(question), ChatResponseBody.class);
        String newTitle = responseBody.getChoices()[0].getMessage().getContent();
        //修改数据库中窗口的标题，同时修改标题状态
        OpenaiChatWindow chatWindow = new OpenaiChatWindow();
        chatWindow.setTitle(newTitle);
        chatWindow.setIsTitleGen(1);
        chatWindow.setWindowId(windowId);
        openaiChatWindowMapper.updateWindowTitleAndStatus(chatWindow);

        return newTitle;
    }

}
