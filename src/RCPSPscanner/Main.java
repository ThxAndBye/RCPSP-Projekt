package RCPSPscanner;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class Main {


    public static void main(String[] args) throws FileNotFoundException {
//		Job[] jobs     = Job.read(new File("j1201_5.sm"));//best makespan=112
//		Resource[] res = Resource.read(new File("j1201_5.sm"));
        Job[] jobs = Job.read(new File("j12046_8.sm"));
        Resource[] res = Resource.read(new File("j12046_8.sm"));

        //Calculate predecessors
        Arrays.stream(jobs).parallel().forEach(job -> job.calculatePredecessors(jobs));

        listJobs(jobs);
        listJobs(res);

        var schedule = new Schedule(jobs, res);
        LinkedList<Integer> plannedJobs = schedule.getSchedule();

        out.println(plannedJobs.parallelStream().map(jobNumber -> String.format("%d, ", jobNumber)).sequential().collect(Collectors.joining()));
        out.printf("Horizon: %d | Makespan: %d%n", schedule.getHorizon(), schedule.getMakespan());
    }


    private static void listJobs(Job[] jobs) {
        var totalDuration = new AtomicInteger();
        var output = new StringBuilder();

        Arrays.stream(jobs).forEach(job -> {
            totalDuration.addAndGet(job.getDuration());
            output.append(String.format("Number: %3d | Successors: %-15s | Predecessors: %-15s | Duration: %2d | R1: %3d  R2: %3d  R3: %3d  R4: %3d %n",
                    job.getNumber(),
                    formatList(job.getSuccessors()),
                    formatList(job.getPredecessors()),
                    job.getDuration(),
                    job.usedResources(0),
                    job.usedResources(1),
                    job.usedResources(2),
                    job.usedResources(3)
            ));
        });

        output.append("\nTotal duration = ").append(totalDuration.get());
        out.println(output);
    }

    private static String formatList(List<Integer> list) {
        var listAsString = new StringBuilder();
        list.forEach(value -> listAsString.append(String.format("%3s  ", String.valueOf(value))));
        if (listAsString.length() == 0) listAsString.append("None");

        return listAsString.toString();
    }

    private static void listJobs(Resource[] resources) {
        var output = new StringBuilder();
        Arrays.stream(resources).forEach(resource -> output
                .append("Resource: ")
                .append(resource.getNumber())
                .append("\t| Availability: ")
                .append(resource.getMaxAvailability())
                .append("\n"));
        out.println(output);
    }


}
