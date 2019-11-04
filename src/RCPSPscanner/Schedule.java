package RCPSPscanner;

import java.util.LinkedList;
import java.util.Optional;

class Schedule {

    private Job[] jobs;

    LinkedList<Integer> getSchedule() {
        return schedule;
    }

    private LinkedList<Integer> schedule = new LinkedList<>();

    Schedule(Job[] jobs) {
        this.jobs = jobs;
        calculateInitialJobList();
    }

    private void calculateInitialJobList() {
        LinkedList<Job> eligibleJobs = new LinkedList<>();

        schedule.add(jobs[0].getNumber());
        jobs[0].getSuccessors().parallelStream().map(dummyJob -> Job.getJob(jobs, dummyJob)).sequential().forEach(eligibleJobs::add);

        Optional<Job> shortest;
        while (!eligibleJobs.isEmpty()) {
            shortest = eligibleJobs.parallelStream().min(Job::compareTo);
            shortest.ifPresent(shortestJob -> schedule.add(shortestJob.getNumber()));

            eligibleJobs = calculateEligibleJobs(jobs, schedule);
        }
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
