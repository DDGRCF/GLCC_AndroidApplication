<div>&nbsp;</div>
<div align="center">
<a href="https://github.com/DDGRCF/GLCC_Server">
    <div>
        <img src="./asserts/logoWithText.png" width = "488" height = "159" alt="logo-catcat" align=center />
    </div>
</a>
<div>&nbsp;</div>
    <div>
        <img src="./asserts/gitlink&&mmdeploy.png" width="" height = "40" alt="logo-mmdeply&&gitlink" align=center/>
    </div>
</div>

<h1 align="center">

Cat Cat | MMDeply $\times$ GLCC 开源项目 | MMDeploy 实践

</h1>


<p align="center">
    <a href='https://github.com/DDGRCF/GLCC_Server'><img src='https://img.shields.io/badge/system-android-blue' alt="System"></a>  
    &nbsp;
    <a href='https://github.com/DDGRCF/GLCC_Server'><img src='https://img.shields.io/badge/role-client-green' alt="Role"></a>
    &nbsp;
    <a href='https://github.com/DDGRCF/GLCC_Server'><img src='https://img.shields.io/badge/build-test-red' alt="Build"></a>  
</p>

# 介绍
✨ 本项目是发起于[CCF GitLink开源编程夏令营](https://www.gitlink.org.cn/)(GitLink Code Camp, 简称GLCC). CCF GitLink 开源编程夏令营（GitLink Code Camp，简称 GLCC），是在 CCF 中国计算机学会指导下，由 GitLink 社区联合 CCF 开源发展委员会（CCF ODC）共同举办的面向全国高校学生的暑期开源项目实习计划。活动将联合各大开源企业、开源基金会、开源社区及开源领域专家，旨在鼓励高校学生通过参与真实的开源软件开发，感受开源文化，提升自身技术能力，进而达到为开源企业和社区输送优秀人才的目的。作为此次活动的特邀合作单位，OpenMMLab 设置了 12 个项目方向共 15 个课题

✨ 本项目基于[OpenMMLab](https://github.com/open-mmlab)下的子开源项目[MMDeploy](https://github.com/open-mmlab/mmdeploy)🔥🔥🔥实现的。MMDeploy 是 OpenMMLab 模型部署工具箱，为各算法库提供统一的部署体验。基于 MMDeploy，开发者可以轻松从训练 repo 生成指定硬件所需 SDK，省去大量适配时间。目前 MMDeploy 支持的算法 repo 有：mmcls、mmdet、mmdet3d、mmedit、mmocr、mmpose、mmseg、mmrazor。MMDeploy 同时支持超多推理后端：ONNX Runtime、TensorRT、ppl.nn、ncnn、OpenVINO，目前新版已经发布，快去体验一下吧🔥🔥🔥

✨ 针对如今猫猫饲养普及，但人们工作越来越忙碌，不能够照看猫猫的问题，本项目基于MMDeploy，实现了猫猫的检测识别、跟踪、视频记录、视频推送等服务，该服务主要具有以下两个功能:

* 能够对猫猫进行检测和跟踪，并将检测结果进行推流，可通过手机App播放
* 能够对猫猫进行划线检测，对于猫猫进入一定区域的时间点的视频进行记录并推送给用户

实际上通过简单的修改配置文件，能够很容易将本服务拓展到其他宠物的检测识别、跟踪服务

目前，该项目包括两个部分:

* **能够响应Http请求，完成检测跟踪的服务器** 👈 **目前位置**
* [能够发起Http请求，完成实现播放的客服端](https://github.com/DDGRCF/GLCC_AndroidApplication)

**✨服务器目前支持的功能:**

- [x] 响应基础的Http请求    
- [x] 猫猫检测和跟踪 
- [x] 猫猫的越线检测
- [x] 猫猫的越线时间点视频的记录
- [x] 猫猫监控的直播视频流的推送
- [x] 记录视频的文件视频流的推送
- [x] 提供配置文件可拓展其他类别
- [x] 猫猫记录视频的定时删除
- [ ] 加载SSL证书 
 
**✨客服端目前完成的功能:**

- [x] 发起基础的Http请求
- [x] 用户登录注册
- [x] 视频源注册删除
- [x] 猫猫监控的直播视频流的拉取
- [x] 猫猫记录的文件视频流的拉取
- [x] 支持服务器配置端口，IP等信息
- [ ] 加载SSL证书

# 预览


## 服务器

![serverInstance](/asserts/serverInstance.gif)

## 客服端
<img src="./asserts/clientInstanceSplash.jpg" width="190" height = "411" alt="server-Instance"/>  <img src="./asserts/clientInstanceLogin.jpg" width="190" height = "411" alt="server-Instance"/>  <img src="./asserts/clientInstanceRegister.jpg" width="190" height = "411" alt="server-Instance"/>  <img src="./asserts/clientInstance.gif" width="190" height = "411" alt="server-Instance"/>

## 说明
|  <div align="center">✨**服务端**✨</div>           |  <div align="center">✨**客户端**✨</div>          |
| -------------------- | -------------------- | 
| 在上面的预览中，我们可以看到服务端运行后，(在客服端发送播放后)跳出来了一个视频界面，视频上两只猫猫被检测出来，并进行了跟踪。另外，我们可以注意到，外面有一个不规则的红色的框。这红色框就代表我们预先设置的栅栏，围栏之类的。猫猫进入了该区域就代表进入了危险区域，那么该红色框就变为实心，并发送通知给用户。这里为了方便演示，我直接将危险框放置在目标中心。值得一提的是，只有猫猫在该区域待够一定时间(可通过配置文件修改)，服务器才会将猫猫识别为进入该危险区域 |  在上面的预览中，我们可以看到在客服端登录后，客服端向服务器发送一个拉流请求并播放相关视频(视频播放界面的纵横比为4/3，如果拉取视频流的纵横比大于或小于这个尺寸，会使用灰色进行填充)，视频下有一个工具栏，工具栏上有**REGISTER**、**DELETE**、**DRAW**、**SOURCE**等字样，分别代表视频流的注册，视频流的删除，放置边界框(栅栏，围栏之类)，目前的播放源功能的按钮(可通过旁边的下拉栏可切换播放源)。然后工具栏下面，有一个可滑动的视频栏目，这个视频栏目就猫猫进入危险时所记录的视频，点击每一个视频栏目可实现视频的播放 dasfsadfasdfsadfsadf |


# 工作流程
<img src="./asserts/glccWorkflow.png" width="800" alt="glcc-workflow"/>

# 安装
### 环境依赖
* **AnroidStudio** ➡️ [点击进入](https://developer.android.com/studio)
### 安装命令
使用最新AndroidStudio打开项目即可

# 开源许可证
本项目采用 [MIT](./LICENSE) 开源许可证

# 感谢以下项目
* [MMDeploy](https://github.com/open-mmlab/mmdeploy.git)
* [Workflow](https://github.com/sogou/workflow/blob/master)
* [MegFlow](https://github.com/MegEngine/MegFlow)
* [AnroidUtilCode](https://github.com/Blankj/AndroidUtilCode)
* [DiglogX](https://github.com/kongzue/DialogX)
* [Fab](https://github.com/Clans/FloatingActionButton)

# 技术交流
<img src="./asserts/weixing.jpg" width="200" alt="weixing"/>