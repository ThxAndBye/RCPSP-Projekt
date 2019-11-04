package RCPSPscanner;
/*
 * Einlese-Programm wurde von Studierenden der HFT Stuttgart entwickelt.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Job implements Comparable<Job> {

    @Override
    public int compareTo(Job job) {
        return this.duration - job.duration;
    }

    // Number of a job
    private int number;

    // successors; each element contains the job-number (int)
    private ArrayList<Integer> successor;

    // predecessors; each element contains the job-number (int)
    private ArrayList<Integer> predecessor;

    // duration of a job
    private int duration;

    // needed resource capacities
    // verwendeteResourcen[0] --> capacities of resource R1
    // verwendeteResourcen[1] --> capacities of resource R2
    // verwendeteResourcen[2] --> capacities of resource R3
    // verwendeteResourcen[3] --> capacities of resource R4
    private int[] usedResources;


    private Job(int number, ArrayList<Integer> successor, int duration, int[] usedResources) {
        this.number = number;
        this.successor = successor;
        this.duration = duration;
        this.usedResources = usedResources;
        this.predecessor = new ArrayList<>();
    }

    int getNumber() {
        return number;
    }

    ArrayList<Integer> getSuccessors() {
        return successor;
    }

    ArrayList<Integer> getPredecessors() {
        return predecessor;
    }

    int getDuration() {
        return duration;
    }

    int usedResources(int i) {
        assert (i >= 0 && i <= 3);
        return usedResources[i];
    }

    public int getAmountOfSuccessors() {
        return successor.size();
    }

    static Job getJob(Job[] jobs, int numberToFind) {
        return Arrays.asList(jobs).parallelStream().filter(job -> job.getNumber() == numberToFind).collect(toSingleton());
    }

    private static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(Collectors.toList(),
                list -> {
                    assert list.size() == 1;
                    return list.get(0);
                }
        );
    }

    void calculatePredecessors(Job[] jobs) {
        Arrays.stream(jobs).forEach(job -> job.getSuccessors().parallelStream()
                .filter(successorNumber -> successorNumber == this.getNumber())
                .sequential().forEach(successorNumber -> this.getPredecessors().add(job.getNumber())));
    }

    static Job[] read(File file) throws FileNotFoundException {

        Scanner scanner = new Scanner(file);
        Job[] jobs = new Job[0];
        int index = 0;

        ArrayList<ArrayList<Integer>> successors = new ArrayList<>();
        boolean startJob = false;

        while (scanner.hasNext()) {
            String nextLine = scanner.nextLine();
            if (nextLine.equals("")) continue;
            Scanner lineScanner = new Scanner(nextLine);
            String nextString = lineScanner.next();

            if (nextString.equals("jobs")) {
                boolean found = false;
                while (!found) {
                    if (lineScanner.next().equals("):")) {
                        int length = lineScanner.nextInt();
                        jobs = new Job[length];
                        for (int i = 0; i < jobs.length; i++) {
                            successors.add(new ArrayList<>());
                        }
                        found = true;
                    }
                }
                continue;
            }
            if (nextString.equals("jobnr.")) startJob = true;
            if (startJob) {
                try {
                    lineScanner.next();
                    if (lineScanner.hasNext()) {
                        lineScanner.next();
                        while (lineScanner.hasNext()) {
                            int suc = Integer.parseInt(lineScanner.next());
                            successors.get(index).add(suc);
                        }
                        index++;
                        if (index == jobs.length) break;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            lineScanner.close();
        }
        index = 0;
        boolean startRequests = false;
        while (scanner.hasNext()) {
            String next = scanner.nextLine();

            if (next.equals("")) continue;
            Scanner lineScanner = new Scanner(next);
            String nextString = lineScanner.next();
            if (!startRequests && lineScanner.hasNext() && lineScanner.next().equals("mode")) startRequests = true;
            if (startRequests) {
                try {
                    int number = Integer.parseInt(nextString);
                    lineScanner.next();
                    int[] res = new int[4];
                    if (lineScanner.hasNext()) {
                        int duration = Integer.parseInt(lineScanner.next());
                        if (lineScanner.hasNext()) {
                            nextString = lineScanner.next();
                            res[0] = Integer.parseInt(nextString);
                            if (lineScanner.hasNext()) {
                                nextString = lineScanner.next();
                                res[1] = Integer.parseInt(nextString);
                                if (lineScanner.hasNext()) {
                                    nextString = lineScanner.next();
                                    res[2] = Integer.parseInt(nextString);
                                    if (lineScanner.hasNext()) {
                                        nextString = lineScanner.next();
                                        res[3] = Integer.parseInt(nextString);
                                    }
                                }
                            }

                        }

                        jobs[index] = new Job(number, successors.get(index), duration, res);
                        index++;
                        if (index == jobs.length) break;
                    }

                } catch (NumberFormatException ignored) {
                }
            }
            lineScanner.close();
        }
        scanner.close();
        return jobs;
    }

}