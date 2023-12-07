当前模块整合的都是文心一言的API，当前具有以下功能:

1. 聊天模块
   - 非流式调用，返回AI回复的JSON字符串
   - 流式调用非流式返回，返回给前端仍然是等待回答结束后再返回
   - 流式调用流式返回，使用SSE和WebSocket技术返回给前端，AI每回复一句就返回一句
2. 图片模块
   - 整合了Stable_Diffusion_XL模型可以生成图片

[SSE如何与前端交互点这里](https://www.yuque.com/autunomy/emwi09/au64kxb17vug4gmc#XCrVK)

[图片生成API使用方式](https://www.yuque.com/autunomy/emwi09/vlnzu17dz08185md)

项目的使用方式:https://www.yuque.com/autunomy/emwi09/xltbkcfuirkye0sn