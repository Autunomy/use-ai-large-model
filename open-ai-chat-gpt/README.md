当前模块整合的都是ChatGPT的API，当前具有以下功能:

1. 聊天模块
    - 非流式调用，返回AI回复的JSON字符串
    - 流式调用非流式返回，返回给前端仍然是等待回答结束后再返回
    - 流式调用流式返回，使用SSE协议
2. 图片模块
    - 图片生成，效果明显好于文心的图像生成
3. 数据库数据存储,表文件就在README.md文件的同级目录中
4. redis作为缓存存储上下文信息

项目教程：https://www.yuque.com/autunomy/emwi09/agrr9v1vo3ntpagw

