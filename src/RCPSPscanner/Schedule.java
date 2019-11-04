package RCPSPscanner;

import java.util.LinkedList;
import java.util.Optional;

class Schedule {

    private Job[] jobs;

    Schedule(Job[] jobs) {
        this.jobs = jobs;
    }

    LinkedList<Integer> calculateInitialJobList() {
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
}
