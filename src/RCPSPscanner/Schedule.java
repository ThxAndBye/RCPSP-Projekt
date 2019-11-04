package RCPSPscanner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.IntStream;

class Schedule {

    private Job[] jobs;
    private  Resource[] res;
    private LinkedList<Integer> schedule = new LinkedList<>();
    private HashMap<Integer, Resource[]> recourceTimeTable = new HashMap<>();

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

    int getMakespan(){
        schedule.parallelStream().map(scheduledJobNumber -> Job.getJob(jobs, scheduledJobNumber)).sequential().forEach(this::schleduleJob);
        return recourceTimeTable.size();
    }

    private void schleduleJob(Job job){
        int startime = starttime(earliestPossibleStarttime(job), job);
        IntStream.rangeClosed(startime, startime + job.getDuration())
                .forEach(timeSlot -> Arrays.stream(recourceTimeTable.get(timeSlot))
                        .forEach(resource -> resource.subtractAvailability(job.usedResources(resource.getNumber() - 1))));
    }

    private int starttime(int earliestStarttime, Job job){
        if (IntStream.rangeClosed(earliestStarttime, earliestStarttime + job.getDuration())
                .mapToObj(requiredTimeSlot -> timeSlotPossible(requiredTimeSlot, job)).noneMatch(val -> val.equals(false))){
         return earliestStarttime;
        } else {
            return starttime(earliestStarttime + 1, job);
        }

    }

    private int earliestPossibleStarttime(Job job){
        Optional<Integer> earliestPossibleStarttime = job.getPredecessors().stream().max(Integer::compareTo);
        return earliestPossibleStarttime.orElse(1);
    }

    private boolean timeSlotPossible(int timeslot, Job job) {
        if (!recourceTimeTable.containsKey(timeslot)) recourceTimeTable.put(timeslot, Resource.toTimetableArray(res));
        return Arrays.stream(recourceTimeTable.get(timeslot)).map(resource -> resource.enoughRecourseForJob(job)).noneMatch(val -> val.equals(false));
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
