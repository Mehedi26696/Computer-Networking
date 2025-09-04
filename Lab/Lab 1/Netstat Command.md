# Netstat Command

## Overview
`netstat` (network statistics) is a command-line tool that displays network connections, routing tables, interface statistics, masquerade connections, and multicast memberships.

## Basic Syntax
```bash
netstat [options]
```

## Common Options

### Display Options
- `-a` : Show all connections (listening and non-listening)
- `-l` : Show only listening ports
- `-n` : Show numerical addresses instead of resolving hosts
- `-p` : Show process ID and name using each socket
- `-t` : Show TCP connections only
- `-u` : Show UDP connections only
- `-r` : Display routing table
- `-i` : Display interface statistics
- `-s` : Display statistics for each protocol

### Useful Combinations
```bash
netstat -tuln    # Show all TCP/UDP listening ports with numerical addresses
netstat -tulpn   # Include process information
netstat -an      # Show all connections with numerical addresses
netstat -rn      # Show routing table with numerical addresses
```

## Output Format
```
Proto Recv-Q Send-Q Local Address    Foreign Address    State       PID/Program
tcp   0      0      0.0.0.0:22       0.0.0.0:*          LISTEN      1234/sshd
tcp   0      0      127.0.0.1:3306   0.0.0.0:*          LISTEN      5678/mysqld
```

## Common Use Cases
- Check which ports are listening: `netstat -ln`
- Find which process is using a port: `netstat -tulpn | grep :PORT`
- Monitor active connections: `netstat -an`
- View network interface statistics: `netstat -i`
- Display routing information: `netstat -rn`

## Connection States
- **LISTEN**: Port is open and listening for connections
- **ESTABLISHED**: Active connection
- **TIME_WAIT**: Connection closed, waiting for remote shutdown
- **CLOSE_WAIT**: Remote end has shut down, waiting for local close