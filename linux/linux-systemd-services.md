# Linux systemd Service Management

## Unit File Anatomy

```ini
# /etc/systemd/system/myapp.service
[Unit]
Description=My Application
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=notify
User=appuser
Group=appuser
WorkingDirectory=/opt/myapp
ExecStart=/opt/myapp/bin/server --config /etc/myapp/config.yaml
ExecReload=/bin/kill -HUP $MAINPID
Restart=on-failure
RestartSec=5
StartLimitBurst=3
StartLimitIntervalSec=60
Environment=NODE_ENV=production
EnvironmentFile=/etc/myapp/env

[Install]
WantedBy=multi-user.target
```

## Service Types

| Type | Behavior |
|------|----------|
| `simple` | Default. Process started by ExecStart is the main process |
| `notify` | Like simple, but service sends notification when ready |
| `forking` | Process forks and parent exits (traditional daemons) |
| `oneshot` | Process exits after doing its work (scripts) |

## Essential Commands

```bash
# Lifecycle
systemctl start myapp
systemctl stop myapp
systemctl restart myapp
systemctl reload myapp        # graceful reload (sends SIGHUP)

# Enable/disable on boot
systemctl enable myapp
systemctl enable --now myapp  # enable + start immediately

# Status and debugging
systemctl status myapp
journalctl -u myapp -f        # follow logs
journalctl -u myapp --since "10 min ago"
systemctl show myapp          # all properties

# After editing unit files
systemctl daemon-reload
```

## Hardening Options

```ini
[Service]
# Filesystem
ProtectSystem=strict          # /usr, /boot, /efi read-only
ProtectHome=true              # /home, /root, /run/user inaccessible
ReadWritePaths=/var/lib/myapp
PrivateTmp=true               # isolated /tmp

# Network
PrivateNetwork=false
RestrictAddressFamilies=AF_INET AF_INET6 AF_UNIX

# Capabilities
NoNewPrivileges=true
CapabilityBoundingSet=CAP_NET_BIND_SERVICE

# System calls
SystemCallFilter=@system-service
```

## Timers (cron replacement)

```ini
# /etc/systemd/system/backup.timer
[Unit]
Description=Run backup daily

[Timer]
OnCalendar=*-*-* 02:00:00
Persistent=true               # run missed executions on boot
RandomizedDelaySec=300

[Install]
WantedBy=timers.target
```

## Useful Patterns

- **Socket activation**: Start service only when connection arrives (`myapp.socket`)
- **Templated units**: `myapp@.service` → `systemctl start myapp@instance1`
- **Drop-in overrides**: `/etc/systemd/system/myapp.service.d/override.conf`
- **Resource limits**: `MemoryMax=512M`, `CPUQuota=50%`

## Debugging Failed Services

```bash
systemctl status myapp                   # quick overview
journalctl -u myapp -n 50 --no-pager    # recent logs
systemctl list-dependencies myapp        # dependency tree
systemd-analyze blame                    # boot time per unit
systemd-analyze security myapp           # security audit
```
