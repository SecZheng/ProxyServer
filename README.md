
constant: ip端口信息

local：本机服务相关信息

remote：有公网ip的服务相关信息

大致流程：

```mermaid
graph LR

A(公网端口1)--请求--> B(公网端口2)--请求-->C(连接公网端口2的本地端口)--请求-->D(本地服务器)
D--响应-->C--响应-->B--响应-->A
```
