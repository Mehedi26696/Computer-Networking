# NSLookup Command

## Introduction

**NSLookup (Name Server Lookup)** is a powerful network administration command-line tool used to query Domain Name System (DNS) servers and obtain domain name or IP address mapping information. It serves as an essential diagnostic utility for network administrators, system administrators, and IT professionals to troubleshoot DNS-related issues and verify DNS configurations.

### Key Objectives

- Query DNS servers for various record types
- Troubleshoot DNS resolution issues
- Verify DNS server configurations
- Perform reverse DNS lookups
- Test DNS propagation and changes

### Primary Functions

- **Forward Lookups**: Domain name → IP address resolution
- **Reverse Lookups**: IP address → Domain name resolution
- **Record Queries**: Retrieve specific DNS record types
- **Server Testing**: Validate DNS server functionality

---

## DNS Fundamentals

### DNS Hierarchy Structure

```
                        Root (.)
                     /     |     \
                   com    org    net    edu
                  /  |  \
               google | yahoo
              /   |   \
            www  mail  ftp
```

### Common DNS Record Types

| **Record** | **Purpose**      | **Example**                           |
| ---------------- | ---------------------- | ------------------------------------------- |
| **A**      | IPv4 address mapping   | `example.com → 192.168.1.1`              |
| **AAAA**   | IPv6 address mapping   | `example.com → 2001:db8::1`              |
| **CNAME**  | Canonical name (alias) | `www → example.com`                      |
| **MX**     | Mail exchange server   | `mail.example.com priority 10`            |
| **NS**     | Name server            | `ns1.example.com`                         |
| **PTR**    | Reverse lookup         | `1.1.168.192.in-addr.arpa → example.com` |
| **TXT**    | Text information       | `"v=spf1 include:_spf.example.com ~all"`  |
| **SOA**    | Start of authority     | Zone configuration data                     |

---

## Command Syntax & Options

### Basic Syntax

```bash
# Non-interactive mode
nslookup [option] [hostname/IP] [DNS-server]

# Interactive mode
nslookup
```

### Command Line Options

| **Option**       | **Description** | **Example**                    |
| ---------------------- | --------------------- | ------------------------------------ |
| `-type=<record>`     | Specify record type   | `nslookup -type=MX example.com`    |
| `-class=<class>`     | Specify query class   | `nslookup -class=IN example.com`   |
| `-timeout=<seconds>` | Set query timeout     | `nslookup -timeout=10 example.com` |
| `-retry=<number>`    | Set retry attempts    | `nslookup -retry=3 example.com`    |
| `-debug`             | Enable debug mode     | `nslookup -debug example.com`      |
| `-port=<port>`       | Specify DNS port      | `nslookup -port=5353 example.com`  |

#### Linux/Unix Systems

```bash
# Standard nslookup
nslookup google.com

# Using specific DNS server
nslookup google.com 8.8.8.8

```

### Interactive Mode Commands

| **Command**       | **Function**      | **Example**  |
| ----------------------- | ----------------------- | ------------------ |
| `set type=<record>`   | Change query type       | `set type=AAAA`  |
| `set class=<class>`   | Change query class      | `set class=CH`   |
| `server <DNS-server>` | Change DNS server       | `server 1.1.1.1` |
| `set debug`           | Enable debug mode       | `set debug`      |
| `set nodebug`         | Disable debug mode      | `set nodebug`    |
| `set timeout=<sec>`   | Set timeout             | `set timeout=30` |
| `help`                | Show available commands | `help`           |
| `exit`                | Exit interactive mode   | `exit`           |

---

## Record Types & Queries

### Detailed Record Type Examples

#### **A Record (IPv4 Address)**

```bash
$ nslookup -type=A google.com
Server:     8.8.8.8
Address:    8.8.8.8#53

Non-authoritative answer:
Name:   google.com
Address: 142.250.191.14
```

#### **AAAA Record (IPv6 Address)**

```bash
$ nslookup -type=AAAA google.com
Server:     8.8.8.8
Address:    8.8.8.8#53

Non-authoritative answer:
google.com  has AAAA address 2607:f8b0:4004:c1b::71
```

---

## Understanding Non-Authoritative Answers

### **What does "Non-Authoritative Answer" mean?**

A **non-authoritative answer** in DNS means the response comes from a DNS server that doesn't have official authority over the queried domain.

### **Authoritative vs Non-Authoritative**

| **Type**              | **Source**             | **Reliability** | **Example**          |
| --------------------------- | ---------------------------- | --------------------- | -------------------------- |
| **Authoritative**     | Domain's official DNS server | Direct from source    | Domain owner's nameserver  |
| **Non-authoritative** | Cached copy from resolver    | May be outdated       | Google DNS (8.8.8.8) cache |

### **Why Non-Authoritative Responses Occur**

```bash
# When you query Google's DNS (8.8.8.8)
$ nslookup google.com 8.8.8.8
Server:     8.8.8.8
Address:    8.8.8.8#53

Non-authoritative answer:  # ← This appears because 8.8.8.8 isn't google.com's official nameserver
Name:   google.com
Address: 142.250.191.14
```

### **How It Works**

1. **Your query** → Google DNS (8.8.8.8)
2. **Google DNS checks cache** → Has recent copy of google.com's records
3. **Returns cached data** → Marked as "non-authoritative"

### **Getting Authoritative Answers**

```bash
# First, find the authoritative nameservers
$ nslookup -type=NS google.com
google.com  nameserver = ns1.google.com

# Then query the authoritative server directly
$ nslookup google.com ns1.google.com
# This would give an authoritative answer (no "non-authoritative" label)
```

**Key Point**: Non-authoritative answers are usually fine for most purposes, but authoritative queries give you the most current, official information directly from the domain owner's DNS servers.

## Summary

### Key Takeaways

| **Aspect**          | **Details**                                           |
| ------------------------- | ----------------------------------------------------------- |
| **Primary Purpose** | Query DNS servers for domain/IP resolution                  |
| **Modes**           | Interactive and  Non-interactive operation                 |
| **Record Types**    | Supports all major DNS record types (A, AAAA, MX, NS, etc.) |
| **Troubleshooting** | Essential tool for DNS issue diagnosis                      |
| **Availability**    | Available on all major operating systems                    |

### Professional Applications

- **Network Troubleshooting**: Diagnose DNS resolution failures
- **Security Analysis**: Investigate suspicious DNS activity
- **System Administration**: Verify DNS configurations
- **Web Development**: Test domain and subdomain setups
- **Performance Monitoring**: Check DNS response times
- **Security Auditing**: Assess DNS infrastructure
