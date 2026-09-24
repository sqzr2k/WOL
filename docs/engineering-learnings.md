# Engineering Learnings

- A WOL Magic Packet can be constructed reliably without an external library and sent over UDP to broadcast, unicast, or relay targets.
- For local and VPN-based targets, the most-specific matching Android network route matters rather than the transport type; system routing remains in control when the selection is ambiguous.
- Android's `InetAddress.isReachable()` can be unreliable depending on the network and device vendor. WOL therefore supplements the check with short TCP connection attempts.
- Current Android versions do not reliably expose ARP or MAC information for other LAN devices. Scan results therefore remain usable even without a MAC address.
- Limiting the LAN scan to a `/24` network and using a semaphore prevents 254 simultaneous probes while keeping the scan cancellable.
- CSV data is parsed and validated in full before Room replaces existing data within a transaction.
