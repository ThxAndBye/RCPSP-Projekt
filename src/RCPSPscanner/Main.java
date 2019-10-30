package RCPSPscanner;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

public class Main {

    private static LinkedList<Job> calculateEligibleJobs(Job[] jobs,LinkedList<Integer> plannedJobs){
        LinkedList<Job> newEligibleJobs = new LinkedList<>();

        for (Integer jobNumber : plannedJobs) {
            Objects.requireNonNull(Job.getJob(jobs, jobNumber)).nachfolger
                    .parallelStream()
                    .map(x -> Job.getJob(jobs, x))
                    .filter(Objects::nonNull)
                    .filter(x -> plannedJobs.containsAll(x.vorgaenger))
                    .filter(x -> !plannedJobs.contains(x.number))
                    .sequential()
                    .forEach(newEligibleJobs::add);
        }

        return newEligibleJobs;
    }


    private  static LinkedList<Integer> calculateInitialJobList(Job[] jobs) {
        LinkedList<Integer> plannedJobs = new LinkedList<>();
        LinkedList<Job> eligibleJobs = new LinkedList<>();

        plannedJobs.add(jobs[0].number);
        jobs[0].nachfolger.parallelStream().map(x -> Job.getJob(jobs, x)).sequential().forEach(eligibleJobs::add);

        Optional<Job> shortest;
        while (!eligibleJobs.isEmpty()) {
            shortest = eligibleJobs.parallelStream().min(Job::compareTo);
            shortest.ifPresent(x -> plannedJobs.add(x.number));

            eligibleJobs = calculateEligibleJobs(jobs, plannedJobs);
        }


        return plannedJobs;
    }


    public static void main(String[] args) throws FileNotFoundException {
//		Job[] jobs     = Job.read(new File("j1201_5.sm"));//best makespan=112
//		Resource[] res = Resource.read(new File("j1201_5.sm"));
        Job[] jobs = Job.read(new File("j12046_8.sm"));
        Resource[] res = Resource.read(new File("j12046_8.sm"));


        for (Job job : jobs) {
            job.calculatePredecessors(jobs);
        }
        auslesen(jobs);
        auslesen(res);
        LinkedList<Integer> plannedJobs;
        plannedJobs = calculateInitialJobList(jobs);
        plannedJobs.forEach(System.out::println);

    }


    private static void auslesen(Job[] jobs) {
        int gesamtDauer = 0;
        for (Job job : jobs) {
            gesamtDauer += job.dauer();

            System.out.print("Nummer: " + job.nummer() + "     |    ");
            System.out.print("Nachfolger: ");
            ArrayList<Integer> nachfolger = job.nachfolger();
            for (Integer integer : nachfolger) {
                System.out.print(" " + integer + " ");

            }
            System.out.print(" Vorgaenger: ");
            ArrayList<Integer> vorgaenger = job.vorgaenger();
            for (Integer integer : vorgaenger) {
                System.out.print(" " + integer + " ");

            }
            System.out.print("     |    ");
            System.out.print("Dauer: " + job.dauer() + "     |    ");
            System.out.println("R1: " + job.verwendeteResource(0) + "  R2: " + job.verwendeteResource(1) +
                    "  R3: " + job.verwendeteResource(2) + "  R4: " + job.verwendeteResource(3));
        }
        System.out.println("T = " + gesamtDauer);
    }

    private static void auslesen(Resource[] resource) {
        for (Resource value : resource) {
            System.out.print("Resource: " + value.nummer() + "     |    ");
            System.out.println("Verf�gbarkeit: " + value.maxVerfuegbarkeit());
        }
    }


}
