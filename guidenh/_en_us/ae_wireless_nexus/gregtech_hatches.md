---
navigation:
  title: "GregTech ME Hatches"
  position: 80
  parent: /ae_wireless_nexus/index.md
  icon: gregtech:gt.blockmachines:2714
---

# GregTech ME Hatches

A GregTech machine gains wireless network selection only when its MetaTileEntity is both an `MTEHatch` and an AE grid
host implementing `IGridProxyable`. Ordinary GregTech machines and non-AE hatches are not changed.

For compatible hatches with a ModularUI2 screen, a wireless-network button appears in the bottom-right control flow.
Its connector icon opens the same selector used by the [ME Wireless Connector](wireless_connector.md). Compatible
hatches without their own machine UI open the selector directly when right-clicked.

## Channels

Wireless connection does not change how many channels the hatch normally requires or can carry. The wireless
controller reserves the hatch's actual AE channel demand.

Cables connected to the hatch continue to follow AE connection rules. A normal-capacity hatch or cable path carries at
most 8 channels; only an endpoint whose AE node has dense capacity can carry up to 32. Devices that cannot carry
channels remain limited to their own required channel.

The selected network, binding player, priority, and last channel demand are saved with the GregTech tile. The link is
released while the tile is unloaded or invalid and is requested again after it becomes active.

## Security and Priority

The same Security Terminal **Build** permission check applies to GregTech hatches. Priorities range from 0 to 100 and
share the same allocation queue as standalone wireless connectors. Higher values are served first; equal values use a
stable dimension-and-position order.
