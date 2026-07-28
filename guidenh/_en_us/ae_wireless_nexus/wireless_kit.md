---
navigation:
  title: "Wireless Kit"
  position: 95
  parent: /ae_wireless_nexus/index.md
  icon: appliedenergistics2:item.ToolWirelessKit
item_ids:
  - appliedenergistics2:item.ToolWirelessKit
---

# Wireless Kit

<ItemImage
  id="appliedenergistics2:item.ToolWirelessKit"
  scale="3"
  label="right"
  format="**%s**"
/>

Wireless Nexus adds a direct binding workflow to AE2's
<ItemLink id="appliedenergistics2:item.ToolWirelessKit" showIcon="true" />. It can select a named wireless network and
bind compatible endpoints without opening their network-selection screens.

## Recipe

<RecipeFor
  id="appliedenergistics2:item.ToolWirelessKit"
  fallbackText="No enabled recipe was found for the AE2 Wireless Kit."
/>

## Select a Network

Hold the Wireless Kit in your main hand and right-click an
[ME Wireless Network Controller](wireless_controller.md). The controller's network is stored on the kit and its name is
shown in the kit's tooltip. Right-clicking another wireless controller replaces the stored selection.

Selecting a network requires **Build** permission on that controller's ME network.

## Bind an Endpoint

With a network selected, right-click either of these endpoints with the Wireless Kit:

* an [ME Wireless Connector](wireless_connector.md), or
* a compatible [GregTech ME hatch](gregtech_hatches.md).

The endpoint binds immediately and its selector GUI does not open. The result message uses the endpoint's localized
name, so GregTech hatches are identified by their own machine name.

Binding also requires **Build** permission on the selected ME network. If the permission check fails, the endpoint is
not rebound.

## Bind While Placing

Put a bound Wireless Kit in the Backhand offhand slot, then place an ME Wireless Connector or a compatible GregTech ME
hatch with your main hand. The newly placed endpoint automatically binds to the network stored on the kit.

Automatic placement binding uses the same **Build** permission check. An unbound kit, a different offhand item, or an
incompatible GregTech machine has no effect on placement.
