---
navigation:
  title: "GregTech ME 仓室"
  position: 80
  parent: /ae_wireless_nexus/index.md
  icon: gregtech:gt.blockmachines:2714
---

# GregTech ME 仓室

只有 MetaTileEntity 同时属于 `MTEHatch` 且是实现了 `IGridProxyable` 的 AE 网格宿主时，该 GregTech 机器才会获得无线网络
选择能力。普通 GregTech 机器和非 AE 仓室不会受到影响。

对于具有 ModularUI2 界面的兼容仓室，无线网络按钮会出现在右下角控件流中。点击其连接器图标会打开与
[ME 无线连接器](wireless_connector.md)相同的选择界面。自身没有机器界面的兼容仓室会在右键时直接打开选择界面。

## 频道

无线连接不会改变仓室原本需要或能够传输的频道数。无线控制器会按照仓室实际的 AE 频道需求进行分配。

连接到仓室的线缆仍遵循 AE 连接规则。普通容量的仓室或线缆路径最多传输 8 个频道；只有 AE 节点具有致密容量的端点才能传输
最多 32 个频道。不能传输频道的设备仍然只会占用其自身所需的频道。

所选网络、绑定玩家、优先级和最近一次频道需求会与 GregTech 方块一同保存。方块卸载或失效时会释放连接，并在重新活动后再次
申请连接。

## 权限和优先级

GregTech 仓室同样使用安全终端的**建造**权限检查。优先级范围为 0 至 100，并与独立无线连接器共用同一个分配队列。数值较高
者优先；数值相同时使用稳定的维度与坐标顺序。
