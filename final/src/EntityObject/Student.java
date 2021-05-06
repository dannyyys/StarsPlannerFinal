package EntityObject;

import Exception.ExistingCourseException;
import Exception.NonExistentCourseException;
import HelperObject.PasswordStorage;
import ValueObject.Gender;
import ValueObject.Nationality;
import ValueObject.School;
import ValueObject.UserType;

import java.util.Calendar;
import java.util.Random;
import java.util.TreeMap;

/**
 * This class represents a Student in NTU during STARS
 */
public class Student extends AbstractUser {
    /**
     * Student matric number
     */
    private String matricNumber;
    /**
     * List of courses registered by the student in form of a TreeMap
     * TreeMap contains course code in String and index of the course in Integer
     */
    private TreeMap<String, Integer> registeredCourses;
    /**
     * List of courses that the student is in waiting list. It is in the form of a TreeMap
     * TreeMap contains course code in String and index of the course in Integer
     */
    private TreeMap<String, Integer> waitingListCourses;
    /**
     * Total AUs registered
     */
    private int totalRegisteredAUs;
    /**
     *
     */
    private static int count = 0;
    /**
     * Maximum number of AUs student can have
     */
    private int maxAUs;

    /**
     * Creates student containing student specific information.
     * Student information to be set by administrative user
     *
     * @param name A String that represents the student name
     * @param school An Enum value of the school that is offering the course
     * @param gender An Enum value of the gender that the student belongs to
     * @param nationality An Enum value of the nationality of the student
     * @param maxAUs An integer thar represents to number of AUs a student can have
     * @param random An object that helps to generate the student's matric number
     * @throws PasswordStorage.CannotPerformOperationException unsafe to hash password
     * @see ValueObject.School
     * @see ValueObject.Gender
     */
    public Student(String name, School school, Gender gender, Nationality nationality, int maxAUs, Random random) throws PasswordStorage.CannotPerformOperationException {
        super(name, school, gender, nationality, UserType.USER);
        int year = (Calendar.getInstance().get(Calendar.YEAR))%100 ;
        this.matricNumber = "U" + year +
                String.format("%05d", count++) +
                (char)(random.nextInt(26) + 'A');
        this.registeredCourses = new TreeMap<>();
        this.waitingListCourses = new TreeMap<>();
        this.totalRegisteredAUs = 0;
        this.maxAUs = maxAUs;
    }

    /**
     * Gets a String that represents the student's matric number
     * @return String that represents student's matric number
     */
    public String getMatricNumber() {
        return matricNumber;
    }

    /**
     * Sets the matric number of the student
     * @param matricNumber A String that represents the student's matric number
     */
    public void setMatricNumber(String matricNumber) {
        this.matricNumber = matricNumber;
    }

    /**
     * Add a course and index to the student registeredCourses TreeMap
     * If the course code exists in student's registeredCourses TreeMap, throw ExistingCourseException
     * Else if the codes code exists in student's waiting list courses TreeMap, delete it from the waitingListCourses TreeMap
     * else add the course code and index to the student's registeredCourses TreeMap
     * @param courseCode A String that represents the course code of the course
     * @param indexNumber An Integer that represents the index number of the course
     * @throws ExistingCourseException course already registered
     */
    public void registerCourse(String courseCode, int indexNumber) throws ExistingCourseException {
        if (registeredCourses.containsKey(courseCode)) {
            throw new ExistingCourseException();
        } else if (waitingListCourses.containsKey(courseCode)) {
            waitingListCourses.remove(courseCode);
            registeredCourses.put(courseCode, indexNumber);
        } else {
            registeredCourses.put(courseCode, indexNumber);
        }
    }

    /**
     * Remove a course code and index from the student registeredCourses TreeMap
     * If the course code exists in student's registeredCourses TreeMap, remove the course and its corresponding index from the student's registeredCourses TreeMap
     * Else if the codes code exists in student's registerWaitListCourse TreeMap, remove the course and its corresponding index from the student's registerWaitListCourse TreeMap
     * else throw NonExistentCourseException
     * @param courseCode A String that represents the course code of the course
     * @throws NonExistentCourseException course not registered
     */
    public void deregisterCourse(String courseCode) throws NonExistentCourseException {
        if (registeredCourses.containsKey(courseCode)) {
            registeredCourses.remove(courseCode);
        } else if (waitingListCourses.containsKey(courseCode)) {
            waitingListCourses.remove(courseCode);
        } else {
            throw new NonExistentCourseException();
        }
    }

    /**
     * Add a course and index to the student's registerWaitListCourse TreeMap
     * If the course code exists in the student's registerWaitListCourse TreeMap, throw ExistingCourseException
     * Else add the course and index into the student's registerWaitListCourse TreeMap
     * @param courseCode A String that represents the course code of the course
     * @param indexNumber An Integer that represents the index number of the course
     * @throws ExistingCourseException course already registered
     */
    public void registerWaitListCourse(String courseCode, int indexNumber) throws ExistingCourseException {
        if (waitingListCourses.containsKey(courseCode)) {
            throw new ExistingCourseException();
        } else {
            waitingListCourses.put(courseCode, indexNumber);
        }
    }

    /**
     * Gets the list of courses that is registered by the student
     * @return A TreeMap containing course codes in String and index of the course in Integer
     */
    public TreeMap<String, Integer> getRegisteredCourses() {
        return registeredCourses;
    }

    /**
     * For a course that student is wait listed in, get TreeMap of course codes and indexes
     * @return A TreeMap as defined above.
     */
    public TreeMap<String, Integer> getWaitingListCourses() {
        return waitingListCourses;
    }

    /**
     * Add AU of a course to the student total AUs
     * @param AUs Integer representing AUs of the course
     */
    public void registerAUs(int AUs) {
        this.totalRegisteredAUs += AUs;
    }

    /**
     * Subtract AU of course that student is dropping from total AU
     * @param AUs Integer representing number of AUs of the course that the student is dropping
     */
    public void deregisterAUs(int AUs) {
        this.totalRegisteredAUs -= AUs;
    }

    /**
     * Gets the Max AU the student can have
     * @return maxAUs Integer of the maximum number of AUs the student can have
     */
    public int getMaxAUs() {
        return maxAUs;
    }

    /**
     * Gets the total registered AUs that the student currently have
     * @return An Integer thats represents the total registered AUs that the student currently have
     */
    public int getTotalRegisteredAUs() {
        return totalRegisteredAUs;
    }

    /**
     * Append student name and matric number into one string
     * @return A String containing the name and matric number
     */
    @Override
    public String toString() {
        return "Name: " + super.getName() + "\t\tMatric Number: " + matricNumber;
    }
}
