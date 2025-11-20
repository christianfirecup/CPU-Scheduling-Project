# CPU Scheduling Project — Round Robin Simulator (README)

This README explains how to run the Round Robin CPU scheduling simulator, the required input format, what the simulator outputs, and how to document the five runs required by the project.

## How to Compile and Run

1. Compile:
   ```bash
   javac RoundRobinSimulator.java
   ```
2. Run with a process CSV file and a time quantum `q` (two parameters):
   ```bash
   java RoundRobinSimulator processes_prompt.csv 4
   ```

## Input File Format

Plaintext CSV with header:
```
pid,arrive,burst
```
- `pid`: process ID (integer)
- `arrive`: arrival time (integer)
- `burst`: service/burst time required (integer)

**Example**
```
pid,arrive,burst
1,0,5
2,1,7
3,0,2
4,2,6
```

## What the Simulator Does (Round Robin)

- **Clock** timestamps all events (arrivals, time slices, completions).
- **Process Creator** inserts processes into a **FIFO ready queue** at their arrival time.
- **CPU** executes the process at the head of the queue for a fixed **time quantum** `q` (or until the process finishes if remaining time < `q`).
- **Preemption**: if a process is not finished after `q`, it is placed at the end of the ready queue.
- **Context Switch Count** is tracked whenever the CPU dispatches a process.
- **Idle Time** is accumulated when no process is ready and the next arrival is in the future.

## Output and Performance Metrics

For each run, the program prints:
- **Event Log** (timestamped)
- **Per-Process Table** with: `pid, arrive, burst, completion, turnaround, waiting, response`
- **Overall Performance Metrics**:
  - **CPU Utilization** = `1 - (idle time / total execution time)`
  - **Throughput** = completed processes per unit time
  - **Average Turnaround Time**
  - **Average Waiting Time**
  - **Average Response Time**

## Results Snapshot (Example Output Block)

From a run on `processes_prompt.csv`:

```
=== Overall Performance Metrics ===
Total processes: 4
Total execution time: 20
Context switches: 4
Context switch time (per switch): 0
Total idle time (incl. context switch time): 0
CPU Utilization: 100.00%
Throughput: 0.2000 processes per time unit
Average Turnaround Time: 10.75
Average Waiting Time: 5.75
Average Response Time: 5.75
```

## Five-Run Summary Table (processes_prompt.csv; q = 1, 2, 4, 7, 12)

| q | Total Time | Ctx Sw | Util (%) | Thruput | Avg TAT | Avg WT | Avg RT |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 20 | 20 | 100.00 | 0.2000 | 14.00 | 9.00 | 1.00 |
| 2 | 20 | 11 | 100.00 | 0.2000 | 13.75 | 8.75 | 2.25 |
| 4 | 20 |  7 | 100.00 | 0.2000 | 14.00 | 9.00 | 4.25 |
| 7 | 20 |  4 | 100.00 | 0.2000 | 10.75 | 5.75 | 5.75 |
| 12 | 20 |  4 | 100.00 | 0.2000 | 10.75 | 5.75 | 5.75 |

