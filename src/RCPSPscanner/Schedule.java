package RCPSPscanner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.IntStream;

class Schedule {

    private Job[] jobs;
    private Resource[] res;
    private LinkedList<Integer> schedule = new LinkedList<>();
    private HashMap<Integer, Resource[]> recourseTimeTable = new HashMap<>();

    Schedule(Job[] jobs, Resource[] res) {
        this.jobs = jobs;
        this.res = res;
        calculateInitialJobList();
    }

    LinkedList<Integer> getSchedule() {
        return schedule;
    }

    int getHorizon() {
        return Arrays.asList(jobs).parallelStream().mapToInt(Job::getDuration).sum();
    }

    int getMakespan() {
        schedule.parallelStream().map(scheduledJobNumber -> Job.getJob(jobs, scheduledJobNumber)).sequential().forEach(this::scheduleJob);
        return recourseTimeTable.size();
    }

    private void scheduleJob(Job job) {
        int startTime = startTime(earliestPossibleStartTime(job), job);
        job.setStartTime(startTime);
        IntStream.rangeClosed(startTime, startTime + job.getDuration())
                .forEach(timeSlot -> Arrays.stream(recourseTimeTable.get(timeSlot)).parallel()
                        .forEach(resource -> resource.subtractAvailability(job.usedResources(resource.getNumber() - 1))));
    }

    private int startTime(int earliestStartTime, Job job) {
        return IntStream.rangeClosed(earliestStartTime, earliestStartTime + job.getDuration())
                .mapToObj(requiredTimeSlot -> isTimeSlotPossible(requiredTimeSlot, job))
                .noneMatch(val -> val.equals(false)) ? earliestStartTime : startTime(earliestStartTime + 1, job);

    }

    private int earliestPossibleStartTime(Job job) {
        Optional<Integer> earliestPossibleStartTime = job.getPredecessors().parallelStream()
                .map(predecessor -> Job.getJob(jobs, predecessor))
                .map(predecessor -> predecessor.getStartTime() + predecessor.getDuration())
                .max(Integer::compareTo);

        return earliestPossibleStartTime.orElse(1);
    }

    private boolean isTimeSlotPossible(int timeSlot, Job job) {
        if (!recourseTimeTable.containsKey(timeSlot)) recourseTimeTable.put(timeSlot, Resource.toTimetableArray(res));
        return Arrays.stream(recourseTimeTable.get(timeSlot)).map(resource -> resource.enoughRecourseForJob(job)).noneMatch(val -> val.equals(false));
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
