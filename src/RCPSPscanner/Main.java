package RCPSPscanner;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public class Main {

    private static ArrayList<Job> calculateEligableJobs(Job[] jobs,LinkedList<Integer> plannedJobs){
        ArrayList<Job> newEligableJobs = new ArrayList<>();

        for (Integer jobNumber : plannedJobs) {
            Job.getJob(jobs, jobNumber).nachfolger.forEach(x -> newEligableJobs.add(Job.getJob(jobs, x)));
        }
        newEligableJobs.forEach(x -> {
            if(!plannedJobs.containsAll(x.vorgaenger)) newEligableJobs.remove(x);
        });

        return newEligableJobs;
    }


    private  static LinkedList calculateInitialJoblist(Job[] jobs) {
        LinkedList<Integer> plannedJobs = new LinkedList<>();
        ArrayList<Job> eligableJobs = new ArrayList<>();
        int count = 0;
        plannedJobs.add(jobs[0].number);
        jobs[0].nachfolger.forEach(x -> eligableJobs.add(Job.getJob(jobs, x)));



        Optional<Job> shortest = eligableJobs.stream().min(Job::compareTo);
        shortest.ifPresent(x -> plannedJobs.add(x.number));
        eligableJobs.remove(shortest);


        return plannedJobs;
    }


    public static void main(String[] args) throws FileNotFoundException {
//		Job[] jobs     = Job.read(new File("j1201_5.sm"));//best makespan=112
//		Resource[] res = Resource.read(new File("j1201_5.sm"));
        Job[] jobs = Job.read(new File("j12046_8.sm"));
        Resource[] res = Resource.read(new File("j12046_8.sm"));


        for (int i = 0; i < jobs.length; i++) {
            jobs[i].calculatePredecessors(jobs);
        }
        auslesen(jobs);
        auslesen(res);
        calculateInitialJoblist(jobs).forEach(System.out::println);

    }


    private static void auslesen(Job[] jobs) {
        int gesamtDauer = 0;
        for (int i = 0; i < jobs.length; i++) {
            gesamtDauer += jobs[i].dauer();

            System.out.print("Nummer: " + jobs[i].nummer() + "     |    ");
            System.out.print("Nachfolger: ");
            ArrayList<Integer> nachfolger = jobs[i].nachfolger();
            for (int j = 0; j < nachfolger.size(); j++) {
                System.out.print(" " + nachfolger.get(j) + " ");

            }
            System.out.print(" Vorgaenger: ");
            ArrayList<Integer> vorgaenger = jobs[i].vorgaenger();
            for (int j = 0; j < vorgaenger.size(); j++) {
                System.out.print(" " + vorgaenger.get(j) + " ");

            }
            System.out.print("     |    ");
            System.out.print("Dauer: " + jobs[i].dauer() + "     |    ");
            System.out.println("R1: " + jobs[i].verwendeteResource(0) + "  R2: " + jobs[i].verwendeteResource(1) +
                    "  R3: " + jobs[i].verwendeteResource(2) + "  R4: " + jobs[i].verwendeteResource(3));
        }
        System.out.println("T = " + gesamtDauer);
    }

    private static void auslesen(Resource[] resource) {
        for (int i = 0; i < resource.length; i++) {
            System.out.print("Resource: " + resource[i].nummer() + "     |    ");
            System.out.println("Verfügbarkeit: " + resource[i].maxVerfuegbarkeit());
        }
    }


}
