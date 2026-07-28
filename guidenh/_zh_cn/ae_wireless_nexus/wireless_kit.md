---
navigation:
  title: "无线连接工具"
  position: 95
  parent: /ae_wireless_nexus/index.md
  icon: appliedenergistics2:item.ToolWirelessKit
item_ids:
  - appliedenergistics2:item.ToolWirelessKit
---

# 无线连接工具

<ItemImage
  id="appliedenergistics2:item.ToolWirelessKit"
  scale="3"
  label="right"
  format="**%s**"
/>

无线枢纽为 AE2 的 <ItemLink id="appliedenergistics2:item.ToolWirelessKit" showIcon="true" />
增加了直接绑定功能。它可以选定一个具名无线网络，并在不打开网络选择界面的情况下绑定兼容端点。

## 配方

<RecipeFor
  id="appliedenergistics2:item.ToolWirelessKit"
  fallbackText="未找到已启用的 AE2 无线连接工具配方。"
/>

## 选择网络

主手持无线连接工具右键 [ME 无线网络控制器](wireless_controller.md)，即可将该控制器的网络保存到工具中。工具的
Tooltip 会显示已选网络名称。右键另一个无线控制器会覆盖原有选择。

选择网络要求玩家拥有该控制器 ME 网络的**建造**权限。

## 绑定端点

工具已选定网络后，手持它右键以下任一端点：

* [ME 无线连接器](wireless_connector.md)；或
* 兼容的 [GregTech ME 仓室](gregtech_hatches.md)。

端点会立即绑定，不会打开网络选择界面。结果消息使用端点自身的本地化名称，因此 GregTech ME 仓室会显示其具体机器名称。

绑定同样要求玩家拥有所选 ME 网络的**建造**权限。权限检查失败时不会改绑该端点。

## 放置时自动绑定

将已选定网络的无线连接工具放入 Backhand 副手栏，然后用主手放置 ME 无线连接器或兼容的 GregTech ME 仓室。新放置的端点会
自动绑定至工具保存的网络。

放置时自动绑定仍会检查**建造**权限。工具未绑定网络、副手持有其他物品或放置的 GregTech 机器不兼容时，均不会触发自动绑定。
