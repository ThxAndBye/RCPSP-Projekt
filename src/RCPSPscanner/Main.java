package RCPSPscanner;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {

    private static LinkedList<Job> calculateEligibleJobs(Job[] jobs, LinkedList<Integer> plannedJobs) {
        LinkedList<Job> newEligibleJobs = new LinkedList<>();

        plannedJobs.forEach(jobNumber -> Job.getJob(jobs, jobNumber).getSuccessors()
                .parallelStream()
                .map(successorNumber -> Job.getJob(jobs, successorNumber))
                .filter(possibleNextJob -> plannedJobs.containsAll(possibleNextJob.getPredecessors()))
                .filter(possibleNextJob -> !plannedJobs.contains(possibleNextJob.getNumber()))
                .sequential()
                .forEach(newEligibleJobs::add));

        return newEligibleJobs;
    }


    private static LinkedList<Integer> calculateInitialJobList(Job[] jobs) {
        LinkedList<Integer> plannedJobs = new LinkedList<>();
        LinkedList<Job> eligibleJobs = new LinkedList<>();

        plannedJobs.add(jobs[0].getNumber());
        jobs[0].getSuccessors().parallelStream().map(dummyJob -> Job.getJob(jobs, dummyJob)).sequential().forEach(eligibleJobs::add);

        Optional<Job> shortest;
        while (!eligibleJobs.isEmpty()) {
            shortest = eligibleJobs.parallelStream().min(Job::compareTo);
            shortest.ifPresent(shortestJob -> plannedJobs.add(shortestJob.getNumber()));

            eligibleJobs = calculateEligibleJobs(jobs, plannedJobs);
        }


        return plannedJobs;
    }


    public static void main(String[] args) throws FileNotFoundException {
//		Job[] jobs     = Job.read(new File("j1201_5.sm"));//best makespan=112
//		Resource[] res = Resource.read(new File("j1201_5.sm"));
        Job[] jobs = Job.read(new File("j12046_8.sm"));
        Resource[] res = Resource.read(new File("j12046_8.sm"));

        //Calculate predecessors
        Arrays.stream(jobs).parallel().forEach(job -> job.calculatePredecessors(jobs));

        listJobs(jobs);
        listJobs(res);

        LinkedList<Integer> plannedJobs = calculateInitialJobList(jobs);
        System.out.println(plannedJobs.parallelStream().map(jobNumber -> jobNumber + ", ").sequential().collect(Collectors.joining()));
    }


    private static void listJobs(Job[] jobs) {
        AtomicInteger totalDuration = new AtomicInteger();
        StringBuilder output = new StringBuilder();

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
        System.out.println(output);
    }

    private static String formatList(List<Integer> list) {
        StringBuilder listAsString = new StringBuilder();
        list.forEach(value -> listAsString.append(String.format("%3s", String.valueOf(value))).append("  "));
        if (listAsString.length() == 0) listAsString.append("None");

        return listAsString.toString();
    }

    private static void listJobs(Resource[] resources) {
        StringBuilder output = new StringBuilder();
        Arrays.stream(resources).forEach(resource -> output
                .append("Resource: ")
                .append(resource.getNumber())
                .append("\t| Availability: ")
                .append(resource.getMaxAvailability())
                .append("\n"));
        System.out.println(output);
    }


}
