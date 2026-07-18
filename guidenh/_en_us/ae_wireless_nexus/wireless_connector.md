---
navigation:
  title: "ME Wireless Connector"
  position: 90
  parent: /ae_wireless_nexus/index.md
  icon: ae_wireless_nexus:wireless_connector
item_ids:
  - ae_wireless_nexus:wireless_connector
---

<BlockImage id="ae_wireless_nexus:wireless_connector" scale="4" />

# ME Wireless Connector

The <ItemLink id="ae_wireless_nexus:wireless_connector" showIcon="true" /> connects a local AE network to a named
wireless network. Right-click it to choose a network that the current player is allowed to access.

The network list follows the target AE network's Security Terminal. A player must have **Build** permission to see and
select that network. The permission is checked again while allocating the connection, so losing access disconnects the
endpoint.

## Channel Behavior

The connector supports up to 32 channels on its wireless link. It reserves only the number of channels actually used
through the link; placing a connector does not immediately consume all 32 channels from the wireless controller.

The connector has dense capacity internally, but its block faces use normal smart-cable connection rules. A normal
cable leaving the connector can therefore carry up to 8 channels, while a dense cable can carry up to 32.

## Priority

The selector accepts a priority from 0 to 100. When wireless capacity is limited, endpoints with a higher priority are
allocated first. Endpoints with the same priority are ordered by dimension and block position, producing a stable and
repeatable result.

An endpoint receives its complete channel request or no lease; requests are not partially allocated.

## Status

The selector reports one of these states:

* **Unbound**: no wireless network is selected.
* **Target offline**: the selected controller network cannot currently be found.
* **No permission**: the binding player no longer has Build permission.
* **Insufficient capacity**: higher-priority allocations have exhausted the wireless channel pool.
* **Connecting**: the endpoint has a lease and is creating its AE grid connection.
* **Connected**: the wireless AE connection is active.

Use **Disconnect** to clear the selected network and release its channel allocation.
