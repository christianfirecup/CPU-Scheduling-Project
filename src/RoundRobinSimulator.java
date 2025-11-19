import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RoundRobinSimulator {

    static class Process {
        int pid;
        int arrivalTime;
        int burstTime;
        int remainingTime;
        int completionTime = -1;
        int firstResponseTime = -1;

        Process(int pid, int arrivalTime, int burstTime) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java RoundRobinSimulator <processFile.csv> <timeQuantum>");
            return;
        }

        String filePath = args[0];
        int timeQuantum;

        try {
            timeQuantum = Integer.parseInt(args[1]);
            if (timeQuantum <= 0) {
                System.out.println("Time quantum must be a positive integer.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Time quantum must be an integer.");
            return;
        }

        List<Process> processes;
        try {
            processes = loadProcessesFromCsv(filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        if (processes.isEmpty()) {
            System.out.println("No valid processes found in file.");
            return;
        }

        simulateRoundRobin(processes, timeQuantum);
    }

    private static List<Process> loadProcessesFromCsv(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<Process> processes = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length < 3) continue;
            try {
                int pid = Integer.parseInt(parts[0].trim());
                int arrive = Integer.parseInt(parts[1].trim());
                int burst = Integer.parseInt(parts[2].trim());
                processes.add(new Process(pid, arrive, burst));
            } catch (NumberFormatException e) {
                continue;
            }
        }
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        return processes;
    }

    private static void simulateRoundRobin(List<Process> processes, int timeQuantum) {
        final int CONTEXT_SWITCH_TIME = 0;

        Queue<Process> readyQueue = new ArrayDeque<>();
        List<Process> notArrived = new ArrayList<>(processes);

        int currentTime = 0;
        int completed = 0;
        int contextSwitches = 0;
        int idleTime = 0;

        List<String> eventLog = new ArrayList<>();

        System.out.println("=== Round Robin CPU Scheduling Simulation ===");
        System.out.println("Time quantum: " + timeQuantum);
        System.out.println("Input processes (pid, arrive, burst):");
        for (Process p : processes) {
            System.out.printf("  P%d: arrive=%d, burst=%d%n", p.pid, p.arrivalTime, p.burstTime);
        }
        System.out.println();

        while (completed < processes.size()) {
            moveArrivedProcesses(notArrived, readyQueue, currentTime, eventLog);

            if (readyQueue.isEmpty()) {
                if (!notArrived.isEmpty()) {
                    int nextArrivalTime = notArrived.get(0).arrivalTime;
                    if (nextArrivalTime > currentTime) {
                        eventLog.add(String.format("[t=%d-%d] CPU idle", currentTime, nextArrivalTime));
                        idleTime += (nextArrivalTime - currentTime);
                        currentTime = nextArrivalTime;
                    } else {
                        currentTime = nextArrivalTime;
                    }
                    continue;
                } else {
                    break;
                }
            }

            Process current = readyQueue.poll();
            if (current == null) continue;

            contextSwitches++;

            if (current.firstResponseTime == -1) {
                current.firstResponseTime = currentTime;
                eventLog.add(String.format("[t=%d] P%d gets CPU first time (response)", currentTime, current.pid));
            } else {
                eventLog.add(String.format("[t=%d] Context switch to P%d", currentTime, current.pid));
            }

            int runFor = Math.min(timeQuantum, current.remainingTime);
            int start = currentTime;
            int end = currentTime + runFor;

            eventLog.add(String.format("[t=%d-%d] P%d runs (remaining before=%d, after=%d)",
                    start, end, current.pid, current.remainingTime, current.remainingTime - runFor));

            current.remainingTime -= runFor;
            currentTime = end;

            moveArrivedProcesses(notArrived, readyQueue, currentTime, eventLog);

            if (current.remainingTime == 0) {
                current.completionTime = currentTime;
                completed++;
                eventLog.add(String.format("[t=%d] P%d completes", currentTime, current.pid));
            } else {
                readyQueue.add(current);
            }
        }

        int totalExecutionTime = currentTime;

        int totalIdleTime = idleTime + contextSwitches * CONTEXT_SWITCH_TIME;
        double cpuUtilization = 1.0 - ((double) totalIdleTime / (double) totalExecutionTime);
        double throughput = (double) processes.size() / (double) totalExecutionTime;

        double totalTurnaround = 0.0;
        double totalWaiting = 0.0;
        double totalResponse = 0.0;

        System.out.println("=== Event Log (timestamped) ===");
        for (String e : eventLog) {
            System.out.println(e);
        }
        System.out.println();

        System.out.println("=== Per-Process Statistics ===");
        System.out.printf("%-5s %-7s %-7s %-12s %-12s %-12s %-12s%n",
                "PID", "Arrive", "Burst", "Completion", "Turnaround", "Waiting", "Response");

        for (Process p : processes) {
            int turnaround = p.completionTime - p.arrivalTime;
            int waiting = turnaround - p.burstTime;
            int response = p.firstResponseTime - p.arrivalTime;

            totalTurnaround += turnaround;
            totalWaiting += waiting;
            totalResponse += response;

            System.out.printf("%-5d %-7d %-7d %-12d %-12d %-12d %-12d%n",
                    p.pid, p.arrivalTime, p.burstTime,
                    p.completionTime, turnaround, waiting, response);
        }

        int n = processes.size();
        double avgTurnaround = totalTurnaround / n;
        double avgWaiting = totalWaiting / n;
        double avgResponse = totalResponse / n;

        System.out.println();
        System.out.println("=== Overall Performance Metrics ===");
        System.out.println("Total processes: " + n);
        System.out.println("Total execution time: " + totalExecutionTime);
        System.out.println("Context switches: " + contextSwitches);
        System.out.println("Context switch time (per switch): " + CONTEXT_SWITCH_TIME);
        System.out.println("Total idle time (incl. context switch time): " + totalIdleTime);
        System.out.printf("CPU Utilization: %.2f%%%n", cpuUtilization * 100.0);
        System.out.printf("Throughput: %.4f processes per time unit%n", throughput);
        System.out.printf("Average Turnaround Time: %.2f%n", avgTurnaround);
        System.out.printf("Average Waiting Time: %.2f%n", avgWaiting);
        System.out.printf("Average Response Time: %.2f%n", avgResponse);
    }

    private static void moveArrivedProcesses(List<Process> notArrived,
                                             Queue<Process> readyQueue,
                                             int currentTime,
                                             List<String> eventLog) {
        Iterator<Process> it = notArrived.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            if (p.arrivalTime <= currentTime) {
                readyQueue.add(p);
                eventLog.add(String.format("[t=%d] P%d arrives (burst=%d)",
                        p.arrivalTime, p.pid, p.burstTime));
                it.remove();
            } else {
                break;
            }
        }
    }
}
