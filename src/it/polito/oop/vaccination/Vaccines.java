/*
    @author: Simone Nicol <en0mia.dev@gmail.com>
    @created: 06/08/21
    @copyright: Check the repository license.
*/

package it.polito.oop.vaccination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Vaccines {

    public final static int CURRENT_YEAR = java.time.LocalDate.now().getYear();
    private final Map<String, Person> people;
    private final List<Interval> intervals;
    private final Map<String, Hub> hubs;
    private int[] hours;
    private BiConsumer<Integer, String> listener = null;
    Set<String> allocated = new HashSet<>();

    public Vaccines() {
    	this.people = new HashMap<>();
    	this.intervals = new LinkedList<>();
    	this.hubs = new HashMap<>();
    	this.hours = new int[7];
    }

    // R1
    /**
     * Add a new person to the vaccination system.
     *
     * Persons are uniquely identified by SSN (italian "codice fiscale")
     *
     * @param firstName first name
     * @param lastName last name
     * @param ssn italian "codice fiscale"
     * @param y birth year
     * @return {@code false} if ssn is duplicate,
     */
    public boolean addPerson(String firstName, String lastName, String ssn, int y) {
    	Person p = this.people.get(ssn);

    	if (p != null) {
    		return false;
    	}

    	p = new Person(ssn, firstName, lastName, y);

    	this.people.put(ssn, p);

        return true;
    }

    /**
     * Count the number of people added to the system
     *
     * @return person count
     */
    public int countPeople() {
    	return this.people.values().size();
    }

    /**
     * Retrieves information about a person.
     * Information is formatted as ssn, last name, and first name
     * separate by {@code ','} (comma).
     *
     * @param ssn "codice fiscale" of person searched
     * @return info about the person
     */
    public String getPerson(String ssn) {
    	Person p = this.people.get(ssn);

    	if (p == null) {
    		return null;
    	}

        return p.toString();
    }

    /**
     * Retrieves of a person given their SSN (codice fiscale).
     *
     * @param ssn "codice fiscale" of person searched
     * @return age of person (in years)
     */
    public int getAge(String ssn) {
    	Person p = this.people.get(ssn);

    	if (p == null) {
    		return -1;
    	}

        return Vaccines.CURRENT_YEAR - p.getYear();
    }

    /**
     * Define the age intervals by providing the breaks between intervals.
     * The first interval always start at 0 (non included in the breaks)
     * and the last interval goes until infinity (not included in the breaks).
     * All intervals are closed on the lower boundary and open at the upper one.
     * <p>
     * For instance {@code setAgeIntervals(40,50,60)}
     * defines four intervals {@code "[0,40)", "[40,50)", "[50,60)", "[60,+)"}.
     *
     * @param breaks the array of breaks
     */
    public void setAgeIntervals(int... breaks) {
    	for (int i = 0; i < breaks.length; i++) {
    		if (i == 0) {
    			this.intervals.add(new Interval(0, breaks[i]));
    			this.intervals.add(new Interval(breaks[i], breaks[i+1]));
    			continue;
    		}

    		if (i == breaks.length - 1) {
    			this.intervals.add(new Interval(breaks[i], "+"));
    			continue;
    		}

    		this.intervals.add(new Interval(breaks[i], breaks[i+1]));
    	}
    }

    /**
     * Retrieves the labels of the age intervals defined.
     *
     * Interval labels are formatted as {@code "[0,10)"},
     * if the upper limit is infinity {@code '+'} is used
     * instead of the number.
     *
     * @return labels of the age intervals
     */
    public Collection<String> getAgeIntervals() {
    	return this.intervals.stream()
    		.sorted()
    		.map(Interval::toString)
    		.collect(Collectors.toList());
    }

    /**
     * Retrieves people in the given interval.
     *
     * The age of the person is computed by subtracting
     * the birth year from current year.
     *
     * @param range age interval label
     * @return collection of SSN of person in the age interval
     */
    public Collection<String> getInInterval(String range) {
    	Interval i = null;
    	List<String> people = new LinkedList<>();

    	for (Interval i1 : this.intervals) {
    		if (i1.toString().equals(range)) {
    			i = i1;
    			break;
    		}
    	}

    	if (i == null) {
    		return null;
    	}

    	for (Person p : this.people.values()) {
    		if (i.between(this.getAge(p.getSsn()))) {
    			people.add(p.getSsn());
    		}
    	}

        return people;
    }

    // R2
    /**
     * Define a vaccination hub
     *
     * @param name name of the hub
     * @throws VaccineException in case of duplicate name
     */
    public void defineHub(String name) throws VaccineException {
    	Hub h = this.hubs.get(name);

    	if (h != null) {
    		throw new VaccineException("Duplicated hub!");
    	}

    	this.hubs.put(name, new Hub(name));
    }

    /**
     * Retrieves hub names
     *
     * @return hub names
     */
    public Collection<String> getHubs() {
    	return this.hubs.values()
    		.stream()
    		.map(Hub::toString)
    		.collect(Collectors.toList());
    }

    /**
     * Define the staffing of a hub in terms of
     * doctors, nurses and other personnel.
     *
     * @param name name of the hub
     * @param countDoctors number of doctors
     * @param nurses number of nurses
     * @param other number of other personnel
     * @throws VaccineException in case of undefined hub, or any number of personnel not greater than 0.
     */
    public void setStaff(String name, int countDoctors, int nurses, int other) throws VaccineException {
    	Hub h = this.hubs.get(name);

    	if (h == null) {
    		throw new VaccineException("Hub not defined!");
    	}

    	if (countDoctors <= 0 || nurses <= 0 || other <= 0) {
    		throw new VaccineException("Invalid param(s)!");
    	}

    	h.setDoctors(countDoctors);
    	h.setNurses(nurses);
    	h.setOther(other);
    }

    /**
     * Estimates the hourly vaccination capacity of a hub
     *
     * The capacity is computed as the minimum among
     * 10*number_doctor, 12*number_nurses, 20*number_other
     *
     * @param hubName name of the hub
     * @return hourly vaccination capacity
     * @throws VaccineException in case of undefined or hub without staff
     */
    public int estimateHourlyCapacity(String hubName) throws VaccineException {
    	Hub h = this.hubs.get(hubName);

    	if (h == null) {
    		throw new VaccineException("Hub not defined");
    	}

    	return h.getHourlyCapacity();
    }

    // R3
    /**
     * Load people information stored in CSV format.
     *
     * The header must start with {@code "SSN,LAST,FIRST"}.
     * All lines must have at least three elements.
     *
     * In case of error in a person line the line is skipped.
     *
     * @param people {@code Reader} for the CSV content
     * @return number of correctly added people
     * @throws IOException in case of IO error
     * @throws VaccineException in case of error in the header
     */
    public long loadPeople(Reader people) throws IOException, VaccineException {
        // Hint:
        BufferedReader br = new BufferedReader(people);

        String line;
        int count = 0;
        int peopleDone = 0;
        String[] split;

        while((line = br.readLine()) != null) {
        	if(count == 0) {
        		if (!line.equals("SSN,LAST,FIRST,YEAR")) {
        			if(this.listener != null) this.listener.accept(1, line);
        			throw new VaccineException("First line incorrect!");
        		}
        		count++;
        		continue;
        	}
        	count++;
        	split = line.split(",");

        	if (split.length != 4) {
        		if(this.listener != null) this.listener.accept(count, line);
        		continue;
        	}

        	String ssn = split[0];
        	String surname = split[1];
        	String name = split[2];
        	int year;

        	try {
        		year = Integer.parseInt(split[3]);
        	} catch (NumberFormatException e) {
        		continue;
        	}

        	if (this.addPerson(name, surname, ssn, year)) {
        		peopleDone++;
        	} else {if(this.listener != null) this.listener.accept(count, line);}
        }

        br.close();
        return peopleDone;
    }

    // R4
    /**
     * Define the amount of working hours for the days of the week.
     *
     * Exactly 7 elements are expected, where the first one correspond to Monday.
     *
     * @param hs workings hours for the 7 days.
     * @throws VaccineException if there are not exactly 7 elements or if the sum of all hours is less than 0 ore greater than 24*7.
     */
    public void setHours(int... hs) throws VaccineException {
    	if (hs.length != 7) {
    		throw new VaccineException("Incorrect hours!");
    	}
    	for (int h : hs) {
    		if (h > 12 || h < 0) {
    			throw new VaccineException("Incorrect hours!");
    		}
    	}

    	this.hours = hs;
    }

    /**
     * Returns the list of standard time slots for all the days of the week.
     *
     * Time slots start at 9:00 and occur every 15 minutes (4 per hour) and
     * they cover the number of working hours defined through method {@link #setHours}.
     * <p>
     * Times are formatted as {@code "09:00"} with both minuts and hours on two
     * digits filled with leading 0.
     * <p>
     * Returns a list with 7 elements, each with the time slots of the corresponding day of the week.
     *
     * @return the list hours for each day of the week
     */
    public List<List<String>> getHours() {
    	List<List<String>> ret = new LinkedList<>();

    	String hours;
    	String minutes;

    	for (int h : this.hours) {
    		List<String> tmp = new LinkedList<>();

    		for (int i = 0; i < h; i++) {
    			for (int j = 0; j < 4; j++) {
    				hours = Integer.toString(i + 9);
    				minutes = Integer.toString(j * 15);
    				if (j == 0) {
    					minutes = "00";
    				}
    				if (i < 10) {
    					hours = "09";
    				}
    				tmp.add(String.format("%s:%s", hours, minutes));
    			}
    		}
    		ret.add(tmp);
    	}

        return ret;
    }

    /**
     * Compute the available vaccination slots for a given hub on a given day of the week
     * <p>
     * The availability is computed as the number of working hours of that day
     * multiplied by the hourly capacity (see {estimateCapacity} of the hub.
     *
     * @return the available vaccination slots for a given hub on a given day of the week
     */
    public int getDailyAvailable(String hubName, int d) {
    	Hub h = this.hubs.get(hubName);

    	if (h == null || d < 0 || d > this.hours.length) {
    		return -1;
    	}

    	try {
    		return h.getHourlyCapacity() * this.hours[d];
    	} catch (VaccineException e) {
    		return -1;
    	}
    }

    /**
     * Compute the available vaccination slots for each hub and for each day of the week
     * <p>
     * The method returns a map that associates the hub names (keys) to the lists
     * of number of available hours for the 7 days.
     * <p>
     * The availability is computed as the number of working hours of that day
     * multiplied by the capacity (see {estimateCapacity} of the hub.
     *
     * @return the available vaccination slots for each hub and for each day of the week
     */
    public Map<String, List<Integer>> getAvailable() {
    	Map<String, List<Integer>> ret = new HashMap<>();

    	for (Hub h : this.hubs.values()) {
    		List<Integer> tmp = new LinkedList<>();
    		for (int i = 0; i < this.hours.length; i++) {
    			tmp.add(this.getDailyAvailable(h.getName(), i));
    		}
    		ret.put(h.getName(), tmp);
    	}

        return ret;
    }

    /**
     * Computes the general allocation plan a hub on a given day.
     * Starting with the oldest age intervals 40%
     * of available places are allocated
     * to persons in that interval before moving the the next
     * interval and considering the remaining places.
     * <p>
     * The returned value is the list of SSNs (codice fiscale) of the
     * persons allocated to that day
     * <p>
     * <b>N.B.</b> no particular order of allocation is guaranteed
     *
     * @param hubName name of the hub
     * @param d day of week index (0 = Monday)
     * @return the list of daily allocations
     */
    public List<String> allocate(String hubName, int d) {
    	Hub h = this.hubs.get(hubName);

    	if (
			h == null
			|| d < 0
			|| d > this.hours.length
		) {
    		return null;
    	}

    	int n = this.getDailyAvailable(hubName, d);

    	List<Interval> intervals = this.intervals.stream()
        		.sorted(Comparator.reverseOrder())
        		.collect(Collectors.toList());

    	List<String> inHubAllocated = new LinkedList<>();

    	for (Interval i : intervals) {
    		long limit = (long) Math.floor(n * 0.4);

    		List<String> people = this.getInInterval(
    				i.toString()).stream()
					.filter(
						(p) -> !allocated.contains(p))
					.collect(Collectors.toList());

    		if (people.size() < limit) {
    			allocated.addAll(people);
    			inHubAllocated.addAll(people);

    			n = n - people.size();
    			continue;
    		}

    		people.stream()
				.limit(limit)
				.forEach(b -> {inHubAllocated.add(b); allocated.add(b);});

    		n = n - (int) limit;
    	}

    	if (n != 0) {
    		for (Interval i : intervals) {
    			if (n <= 0) {
    				break;
    			}

    			List<String> people = new LinkedList<>(this.getInInterval(i.toString()));
				AtomicInteger inserted = new AtomicInteger();
    			people.stream()
					.filter((p) -> !allocated.contains(p))
					.limit(n)
					.forEach(b -> {
						inHubAllocated.add(b);
						allocated.add(b);
						inserted.getAndIncrement();
					});

    			n = n - inserted.get();
    		}
    	}

        return inHubAllocated;
    }

    /**
     * Removes all people from allocation lists and
     * clears their allocation status
     */
    public void clearAllocation() {
    	this.allocated.clear();
    }

    /**
     * Computes the general allocation plan for the week.
     * For every day, starting with the oldest age intervals
     * 40% available places are allocated
     * to persons in that interval before moving the the next
     * interval and considering the remaining places.
     * <p>
     * The returned value is a list with 7 elements, one
     * for every day of the week, each element is a map that
     * links the name of each hub to the list of SSNs (codice fiscale)
     * of the persons allocated to that day in that hub
     * <p>
     * <b>N.B.</b> no particular order of allocation is guaranteed
     * but the same invocation (after {@link #clearAllocation}) must return the same
     * allocation.
     *
     * @return the list of daily allocations
     */
    public List<Map<String, List<String>>> weekAllocate() {
    	List<Map<String, List<String>>> ret = new LinkedList<>();

    	for (int i = 0; i < 7; i++) {
    		Map<String, List<String>> tmp = new HashMap<>();

    		for (String hub : this.hubs.keySet()) {
				tmp.put(hub, this.allocate(hub, i));
			}

    		ret.add(tmp);
    	}
        return ret;
    }

    // R5
    /**
     * Returns the proportion of allocated people
     * w.r.t. the total number of persons added
     * in the system
     *
     * @return proportion of allocated people
     */
    public double propAllocated() {
        return (double) this.allocated.size() / this.people.size();
    }

    /**
     * Returns the proportion of allocated people
     * w.r.t. the total number of persons added
     * in the system, divided by age interval.
     * <p>
     * The map associates the age interval label
     * to the proportion of allocates people in that interval
     *
     * @return proportion of allocated people by age interval
     */
    public Map<String, Double> propAllocatedAge() {
    	Map<String, Double> ret = new HashMap<>();
    	for (Interval i : this.intervals) {
    		List<String> res = new LinkedList<>(
    				this.getInInterval(i.toString())
			);
    		ret.put(
				i.toString(),
				(double) res.stream()
					.filter(b -> this.allocated.contains(b))
					.count() / this.people.size());
    	}
        return ret;
    }

    /**
     * Retrieves the distribution of allocated persons
     * among the different age intervals.
     * <p>
     * For each age intervals the map reports the
     * proportion of allocated persons in the corresponding
     * interval w.r.t the total number of allocated persons
     *
     * @return the distribution of allocated persons among the different age intervals
     */
    public Map<String, Double> distributionAllocated() {
    	Map<String, Double> ret = new HashMap<>();
    	for (Interval i : this.intervals) {
			ret.put(i.toString(),
				(double) this.getInInterval(
					i.toString()).stream()
					.filter(b -> this.allocated.contains(b))
					.count() / this.allocated.size());
		}
        return ret;
    }

    // R6
    /**
     * Defines a listener for the file loading method.
     * The {accept()} method of the listener is called
     * passing the line number and the offending line.
     * <p>
     * Lines start at 1 with the header line.
     *
     * @param lst the listener for load errors
     */
    public void setLoadListener(BiConsumer<Integer, String> lst) {
    	this.listener = lst;
    }
}
