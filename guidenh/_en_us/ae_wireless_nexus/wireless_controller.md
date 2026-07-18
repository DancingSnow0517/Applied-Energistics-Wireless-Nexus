---
navigation:
  title: "ME Wireless Network Controller"
  position: 100
  parent: /ae_wireless_nexus/index.md
  icon: ae_wireless_nexus:wireless_controller
item_ids:
  - ae_wireless_nexus:wireless_controller
---

<BlockImage id="ae_wireless_nexus:wireless_controller" scale="4" />

# ME Wireless Network Controller

The <ItemLink id="ae_wireless_nexus:wireless_controller" showIcon="true" /> replaces one block in a normal AE
[Controller](appliedenergistics2:/items-blocks/controller.md) multiblock and publishes that controller network for
wireless connections. It behaves as part of the controller multiblock, including the normal controller size and shape
rules.

Only one wireless controller may exist in a connected controller multiblock. Placing a second one puts the controller
network into the same conflict state as an invalid AE controller structure and disables its wireless capacity.

## Network Name

Right-click the wireless controller to open its configuration screen. Each controller receives a persistent UUID when
placed and a default name based on that UUID. The visible name can contain up to 32 printable characters, including
non-ASCII text. The UUID remains the internal identity, so renaming a network does not break existing links.

The screen also shows the total, allocated, and available wireless channels.

## Wireless Channel Capacity

Every exposed face of every controller block in the connected multiblock can contribute 32 wireless channels. Capacity
is calculated for the complete connected group, including normal, self-powered, and wireless controller variants.

A face contributes no wireless capacity when the neighboring position is:

* another controller block in the same multiblock, or
* an AE grid host, including AE devices and cable parts that occupy that face.

Air and non-AE blocks do not block the face. For example, a single isolated wireless controller has six exposed faces
and can provide up to 192 wireless channels. Adding AE equipment directly against a face removes that face's 32-channel
contribution.

The capacity is a pool reserved by wireless endpoints. It is separate from the channel count carried by any individual
cable attached to the remote endpoint.

## Availability

The network is unavailable while its controller multiblock is offline, has an invalid controller shape, or contains
more than one wireless controller. Removing the wireless controller also removes its saved network record and releases
its wireless links.
