package RCPSPscanner;
/*
 * Einlese-Programm wurde von Studierende der HFT Stuttgart entwickelt 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Job implements Comparable<Job> {



	// Number of a job
	int number;
	
	// successors; each element contains the job-number (int)
	ArrayList<Integer> nachfolger;
	
	// predecessors; each element contains the job-number (int)
	ArrayList<Integer> vorgaenger;
	
	// duration of a job
	private int dauer;
	
	// needed resource capacities  
	// verwendeteResourcen[0] --> capacities of resource R1
	// verwendeteResourcen[1] --> capacities of resource R2
	// verwendeteResourcen[2] --> capacities of resource R3
	// verwendeteResourcen[3] --> capacities of resource R4
	private int[] verwendeteResourcen;
	
	
	
	
	private Job(int number, ArrayList<Integer> nachfolger, int dauer, int[] verwendeteResourcen){
		this.number = number;
		this.nachfolger = nachfolger;
		this.dauer = dauer;
		this.verwendeteResourcen = verwendeteResourcen;
		this.vorgaenger = new ArrayList<>();
	}
	
	int nummer(){
		return number;
	}
	
	ArrayList<Integer> nachfolger(){
		return nachfolger;
	}
	ArrayList<Integer> vorgaenger(){
		return vorgaenger;
	}
	int dauer(){
		return dauer;
	}
	
	int verwendeteResource(int i){
		if(i >= 0 && i <= 3)
			return verwendeteResourcen[i];
		else
			throw new IllegalArgumentException("Parameter muss zwischen 0 und 3 sein!");
	}
	
	public int anzahlNachfolger(){
		return nachfolger.size();
	}
	
	static Job getJob(Job[] jobs, int nummer){
		ArrayList<Job> jobList = new ArrayList<>(Arrays.asList(jobs));
		return jobList.parallelStream().filter(x -> x.number == nummer).collect(toSingleton());
	}

	void calculatePredecessors(Job[] jobs){
		for (Job job: jobs) {
			for (Integer successorNumber: job.nachfolger()) {
				if(successorNumber == this.number){
					this.vorgaenger.add(job.number);
				}
			}
		}
	}

	static Job[] read(File file) throws FileNotFoundException {
		
		Scanner scanner = new Scanner(file);
		Job[] jobs = new Job[0];
		int index = 0;
		
		ArrayList<ArrayList<Integer>> successors = new ArrayList<>();
		boolean startJob = false;
		
		while(scanner.hasNext()) {
			String nextLine = scanner.nextLine();
			if(nextLine.equals("")){
				continue;
			}
			Scanner lineScanner = new Scanner(nextLine);
			String nextString = lineScanner.next();
			
			if (nextString.equals("jobs")) {
				boolean found = false;
				while(!found){
					if(lineScanner.next().equals("):")){
						int length = lineScanner.nextInt();
						jobs = new Job[length];
						for(int i = 0; i < jobs.length;i++){
							successors.add(new ArrayList<>());
						}
						found = true;
					}
				}
				continue;
			} 	
			if(nextString.equals("jobnr.")){
				startJob = true;
			}
			if(startJob){
				try {
					lineScanner.next();
					if (lineScanner.hasNext()) {
						lineScanner.next();
						while (lineScanner.hasNext()) {
							int suc = Integer.parseInt(lineScanner.next());
							successors.get(index).add(suc);								
						}	
						index++;
						if(index == jobs.length){
							break;
						}
					}	
				} catch (NumberFormatException ignored) {}
			}	
			lineScanner.close();	
		}	
		index = 0;
		boolean startRequests = false;
		while(scanner.hasNext()) {
			String next = scanner.nextLine();
			
			if(next.equals("")){
				continue;
			}
			Scanner lineScanner = new Scanner(next);
			String nextString = lineScanner.next();
			if(!startRequests && lineScanner.hasNext()){
				if(lineScanner.next().equals("mode")){
					startRequests = true;
				}
			}
			if(startRequests){
				try {
					int nummer = Integer.parseInt(nextString);
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
						
						jobs[index] = new Job(nummer, successors.get(index),duration, res);
						index++;
						if(index == jobs.length){
							break;
						}
					}	
					
				} catch (NumberFormatException ignored) {}
			}	
			lineScanner.close();	
		}
		scanner.close();
		return jobs;
	}

	@Override
	public int compareTo(Job job) {
		return this.dauer - job.dauer;
	}

	private static <T> Collector<T, ?, T> toSingleton() {
		return Collectors.collectingAndThen(
				Collectors.toList(),
				list -> {
					if (list.size() != 1) {
						throw new IllegalStateException();
					}
					return list.get(0);
				}
		);
	}
}