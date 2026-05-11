# Linux Performance Troubleshooting — Essential Commands

## The USE Method Checklist

For each resource (CPU, memory, disk, network), check **Utilization, Saturation, Errors**.

## CPU

```bash
# Overall CPU usage
top -bn1 | head -5
# or
htop

# Per-CPU usage
mpstat -P ALL 1 5

# Process-level CPU
ps aux --sort=-%cpu | head -20
# or
top -bn1 -o %CPU | head -20

# Load average (1, 5, 15 min)
uptime
# Load avg > number of CPU cores = saturated
# 4 cores + load avg 6.0 = overloaded

# CPU-bound process analysis
pidstat -u 1 5    # CPU usage per process, every 1 sec, 5 samples
strace -p <PID>   # System calls (warning: high overhead)
```

### Interpreting Load Average
```
CPU cores: 4

Load 1.0: 25% utilized (one core busy)
Load 4.0: 100% utilized (all cores busy, no queue)
Load 8.0: 200% — 4 processes waiting in queue!

Rule: Load / cores > 1.0 = problem
```

## Memory

```bash
# Memory overview
free -h
#              total   used   free   shared  buff/cache  available
# Mem:         16Gi    8Gi    1Gi    200Mi   7Gi         7Gi
#
# "available" is what matters, not "free"
# buff/cache is reclaimable memory

# Top memory consumers
ps aux --sort=-%mem | head -20

# Detailed process memory
pmap -x <PID>

# Check for OOM kills
dmesg | grep -i "out of memory"
journalctl -k | grep -i oom

# Swap usage (swap = problem!)
swapon --summary
vmstat 1 5        # si/so columns = swap in/out
```

### Key Insight
```
"Free" memory is not wasted memory!
Linux uses free RAM for disk cache (buff/cache).
This is GOOD — it speeds up disk reads.
"Available" = free + reclaimable cache = actual available memory.
```

## Disk

```bash
# Disk space
df -h

# Disk I/O statistics
iostat -x 1 5
# %util > 90% = saturated
# await > 10ms = slow (for SSD; HDD ~20ms is normal)

# I/O per process
iotop -o    # Only show processes doing I/O

# Find large files
du -sh /* 2>/dev/null | sort -rh | head -20
find / -type f -size +100M -exec ls -lh {} \;

# Inode usage (can run out of inodes before space!)
df -i
```

## Network

```bash
# Active connections
ss -tuln          # Listening ports
ss -tun           # Active connections
ss -s             # Connection summary

# Bandwidth usage per interface
sar -n DEV 1 5

# Bandwidth per process
nethogs

# DNS resolution test
dig +short example.com
nslookup example.com

# TCP connection states
ss -tan | awk '{print $1}' | sort | uniq -c | sort -rn
# Too many TIME_WAIT = connection churn
# Too many CLOSE_WAIT = application not closing connections

# Test connectivity
curl -w "\n  dns: %{time_namelookup}s\n  connect: %{time_connect}s\n  tls: %{time_appconnect}s\n  total: %{time_total}s\n" -o /dev/null -s https://api.example.com
```

## Quick Diagnostic Checklist

```bash
# 60-second analysis (Brendan Gregg's checklist)
uptime                      # Load average
dmesg | tail -20            # Kernel errors (OOM, hardware)
vmstat 1 5                  # System-wide stats
mpstat -P ALL 1 3           # Per-CPU balance
pidstat 1 5                 # Process-level CPU
iostat -xz 1 3              # Disk I/O
free -m                     # Memory
sar -n DEV 1 3              # Network throughput
sar -n TCP,ETCP 1 3         # TCP stats
top -bn1 | head -20         # Top processes
```

## Common Problems & Solutions

| Symptom | Check | Likely Cause |
|---------|-------|-------------|
| High load, low CPU | `vmstat` (b column) | I/O wait (slow disk) |
| High load, high CPU | `top`, `pidstat` | CPU-bound process |
| OOM kills | `dmesg \| grep oom` | Memory leak, insufficient RAM |
| Slow disk | `iostat` (%util, await) | Disk saturation, failing disk |
| Connection refused | `ss -tuln` | Service not listening |
| Slow network | `curl -w` | DNS, TLS, or server latency |
| Disk full | `df -h`, `du -sh` | Log files, temp files |
| Too many open files | `ulimit -n`, `lsof` | File descriptor exhaustion |

## Interview Answer

> "For Linux performance issues, I follow the USE method: check Utilization, Saturation, and Errors for each resource. I start with `uptime` for load average, `free -h` for memory (checking 'available' not 'free'), `iostat -x` for disk I/O saturation, and `ss -s` for network connections. For process-level analysis, `pidstat` for CPU and `iotop` for disk. The most common issues I see are: high I/O wait (check iostat %util), memory pressure leading to OOM kills (check dmesg), and file descriptor exhaustion (check ulimit). On Kubernetes nodes, I also check `kubectl top nodes` and `kubectl describe node` for resource pressure conditions."
