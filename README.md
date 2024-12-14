# JunkTransfer

## Overview
JunkTransfer is a rate-limited file transfer system designed for distributed environments. It facilitates efficient transfer of large files to multiple clients while adhering to user-defined bandwidth constraints. By utilizing the leaky bucket algorithm for rate-limiting and real-time compression, JunkTransfer optimizes bandwidth usage and minimizes data transmission overhead.

---

## Features
- **Multi-Client Support**: Concurrently transfers file chunks to multiple clients.
- **Rate-Limiting**: Implements a token bucket algorithm for bandwidth control.
- **Real-Time Compression**: Utilizes `CompressedBlockOutputStream` for on-the-fly compression.
- **Thread-Safe Implementation**: Ensures safety in concurrent operations with shared resources.
- **Optimized Performance**: Uses buffered streams and a low-granular timer for efficient CPU and bandwidth utilization.

---

## Dependencies
- **JDK 1.8**
- **Ant Build System**
- **External Libraries**:
    - [Google Guava](https://github.com/google/guava)
    - [Philip Isenhour CompressedBlockOutputStream](http://example-link-for-library.com)

---

## Build Instructions
To compile the project, use the following commands:
```bash
$ cd JunkTransfer
$ ant clean all
```

## Run Instructions

The application can run in two modes: server (-s) and client (-c).

## Server Mode
Start the server with the following command:

```
$ java -classpath "lib/*:out/production/JunkTransfer" \
net.amirrazmjou.Main -s <file_path> <max_transfer_rate_kbps> <ip:port> ...
```

Example:
```
$ java -classpath "lib/*:out/production/JunkTransfer" \
net.amirrazmjou.Main -s data/test_large 10240 192.168.126.1:6666 192.168.126.1:6667
```

Client Mode
Start a client with the following command:
```
$ java -classpath "lib/*:out/production/JunkTransfer" \
net.amirrazmjou.Main -c <port_number>
```

Example:
```
$ java -classpath "lib/*:out/production/JunkTransfer" \
net.amirrazmjou.Main -c 6666
```


How It Works
## How It Works
1. **Server**:
    - Splits the file into fixed-size chunks.
    - Transfers chunks to connected clients using threads.
    - Controls bandwidth using a shared `TokenBucket` among threads.
2. **Client**:
    - Receives file chunks from the server.
    - Writes data to a local file.
3. **Rate-Limiting**:
    - `TokenBucket` ensures bandwidth does not exceed the user-defined rate.
    - Shared across threads for uniform bandwidth distribution.
4. **Compression**:
    - Compresses chunks in real-time during transmission to reduce network overhead.

## Testing
The following methods were used to test the solution:
- **Loopback Interfaces**:
    - Simulated multiple clients on a single machine by creating loopback interfaces with unique IP addresses.
- **Monitoring Tools**:
    - Used Wireshark, tcpdump, and iftop to monitor bandwidth and data flow.
- **Failure Handling**:
    - Tested scenarios where clients were interrupted mid-transfer to ensure the server adjusted bandwidth allocation.
- **Virtual Machines**:
    - Created a virtualized test environment using Vagrant to simulate distributed nodes.

## Design Details
1. **Leaky Bucket Algorithm**:
    - Limits the server's bandwidth by ensuring token consumption for every byte sent.
    - Refills tokens at a fixed interval to regulate traffic.
2. **SpeedLimitedOutputStream**:
    - Ensures rate-limiting by sharing a `TokenBucket` across multiple streams.
    - Allows configurable burst sizes to balance CPU usage and traffic burstiness.
3. **Compression**:
    - Utilizes Philip Isenhour's `CompressedBlockOutputStream` to compress data in small chunks (1 KB).
    - Optimizes the trade-off between CPU load and network efficiency.

---

## Performance
- **Bandwidth Efficiency**:
    - Ensures the server's bandwidth usage matches user-defined limits, even during client interruptions.
- **Compression**:
    - Achieves high compression ratios based on data entropy, reducing communication overhead.
- **Optimized Streams**:
    - Buffered streams accelerate I/O operations, minimizing latency.
    - Fine-grained token consumption enhances responsiveness and even distribution.

---

## Limitations
- The project currently supports IPv4 addresses only.
- Performance may degrade on systems with limited CPU resources due to real-time compression.

---

## Future Improvements
- Add support for IPv6 addresses.
- Provide a graphical user interface for easier usage.
- Introduce encryption to secure data transmission.

---

## License
This project is licensed under the MIT License. See the LICENSE file for details.

---

## Contribution
Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature-name`).
3. Commit your changes (`git commit -m "Add feature"`).
4. Push to the branch (`git push origin feature-name`).
5. Open a Pull Request.

---

## Author
Developed by **Amir Razmjou**.

## Vagrantfile Usage
A `Vagrantfile` is provided to help you set up a virtualized environment for testing the application. Follow these steps to use it:

### Prerequisites
- Install [Vagrant](https://www.vagrantup.com/).
- Install a compatible virtualization provider such as VirtualBox.

### Instructions
1. Navigate to the project directory containing the `Vagrantfile`:
```bash
   $ cd JunkTransfer
```