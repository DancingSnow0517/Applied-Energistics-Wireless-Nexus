---
navigation:
  title: "ME 无线网络控制器"
  position: 100
  parent: /ae_wireless_nexus/index.md
  icon: ae_wireless_nexus:wireless_controller
item_ids:
  - ae_wireless_nexus:wireless_controller
---

<BlockImage id="ae_wireless_nexus:wireless_controller" scale="4" />

# ME 无线网络控制器

<ItemLink id="ae_wireless_nexus:wireless_controller" showIcon="true" /> 可替代普通 AE
[控制器](appliedenergistics2:/items-blocks/controller.md)多方块中的一个方块，并将该控制器网络发布为无线网络。它本身是控制器
多方块的一部分，因而同样遵循原有的控制器尺寸与形状规则。

一个相连的控制器多方块中只能存在一个无线控制器。放置第二个无线控制器会使该网络进入与无效 AE 控制器结构相同的冲突状态，
并禁用其无线频道容量。

## 网络名称

右键无线控制器可打开配置界面。每个控制器放置时都会获得一个持久 UUID，并以该 UUID 生成默认名称。可见名称最多包含 32 个
可打印字符，支持中文等非 ASCII 文本。UUID 始终作为内部唯一标识，因此重命名不会破坏已有连接。

该界面还会显示无线频道总数、已分配频道数和可用频道数。

## 无线频道容量

相连多方块中每个控制器方块的每个外露面均可提供 32 个无线频道。容量会按完整控制器群计算，其中包括普通、自充能和无线控制器
变体。

遇到以下情况时，该面不提供无线容量：

* 相邻位置是同一多方块中的另一个控制器方块；或
* 相邻位置存在 AE 网格宿主，包括占据该面的 AE 设备和线缆部件。

空气和非 AE 方块不会阻挡该面。例如，一个独立无线控制器有六个外露面，最多可提供 192 个无线频道。在任一面紧贴 AE 设备会
使该面不再贡献 32 个频道。

这些容量构成供无线端点申请的频道池，与远端任意一条线缆自身可承载的频道数相互独立。

## 可用状态

控制器多方块离线、形状无效或包含多个无线控制器时，该无线网络不可用。移除无线控制器还会删除其存档网络记录并释放所有无线
连接。
