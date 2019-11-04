package RCPSPscanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

class Resource {

    // Number of a resource
    private int number;

    // Maximum availability
    private int maxAvailability;
    private int availibility;

    public void addAvailibility(int value) {
        availibility += value;
    }

    void substractAvailibility(int value) {
        availibility -= value;
    }

    public int getAvailibility() {
        return availibility;
    }

    boolean enoughRecourceForJob(Job job){
        return this.availibility >= job.usedResources(this.number - 1);
    }

    private Resource(int availability, int number) {
        this.maxAvailability = availability;
        this.number = number;
    }

    private Resource(Resource resource) {
        this.maxAvailability = resource.maxAvailability;
        this.availibility = resource.maxAvailability;
        this.number = resource.number;
    }

    static Resource[] toTimetableArray(Resource[] res){
        return Arrays.stream(res).map(Resource::new).toArray(Resource[]::new);
    }

    int getMaxAvailability() {
        return maxAvailability;
    }

    int getNumber() {
        return number;
    }

    static Resource[] read(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        Resource[] resources = new Resource[4];
        boolean found = false;
        while (scanner.hasNext()) {
            String nextLine = scanner.nextLine();
            if (nextLine.equals("")) continue;
            Scanner next = new Scanner(nextLine);
            String nextString = next.next();

            if (!found && nextString.equals("R")) {
                found = true;
                continue;
            }
            if (found) {
                resources[0] = new Resource(Integer.parseInt(nextString), 1);
                if (next.hasNext()) {
                    resources[1] = new Resource(next.nextInt(), 2);
                    if (next.hasNext()) {
                        resources[2] = new Resource(next.nextInt(), 3);
                        if (next.hasNext()) {
                            resources[3] = new Resource(next.nextInt(), 4);
                        }
                    }
                }
                break;
            }
            next.close();
        }
        scanner.close();
        return resources;
    }
}
