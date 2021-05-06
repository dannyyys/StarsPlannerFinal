package EntityObject;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.*;

import Exception.NonExistentIndexException;
import ValueObject.DayOfWeek;
import ValueObject.School;
import ValueObject.Venue;

/**
 * Represents a course available to students
 */
public class Course implements Serializable {
    /**
     * course code
     */
    private final String courseCode;
    /**
     * name of the course
     */
    private String courseName;
    /**
     * School that the course belongs to
     */
    private School school;
    /**
     * Lecture timing of this course
     */
    private final Hashtable<DayOfWeek, List<LocalTime>> lectureTimings;
    /**
     * Venue of the lecture
     */
    private Venue lectureVenue;
    /**
     * Number of AUs for this course
     */
    private final int AUs;
    /**
     * Indexes available for this course
     */
    private TreeMap<Integer, Index> indexes;
    /**
     * Serialised ID that is tagged to the course object
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a course object containing course specific information.
     * Course information to be set by administrative user
     *
     * @param courseCode A String containing the unique identifier of the course with 2 alphabets followed by 4 digits.
     * @param courseName A String containing the name of the course
     * @param school An Enum value of the school that is offering the course
     * @param lectureTimings A Hashtable with the lecture day and the list of start and end timings of a lecture session
     * 			as the key and value respectively
     * @param lectureVenue An Enum value of the lecture venue for all lecture sessions
     * @param AUs An integer that represents the number of academic units of the course
     * @param indexes A TreeMap with an index number and its corresponding index object as key and value respectively
     * @see ValueObject.School
     * @see ValueObject.Venue
     */
    public Course(String courseCode, String courseName, School school, Hashtable<DayOfWeek, List<LocalTime>> lectureTimings, Venue lectureVenue, int AUs, ArrayList<Index> indexes) {
        this.courseCode = courseCode.toLowerCase();
        this.courseName = courseName;
        this.school = school;
        this.lectureTimings = lectureTimings;
        this.lectureVenue = lectureVenue;
        this.AUs = AUs;
        this.indexes = new TreeMap<>();
        for (Index index : indexes) {
            this.indexes.put(index.getIndexNumber(), index);
        }
    }

    /** Gets the course code of the course object.
     *
     * @return A String containing the course code
     */
    public String getCourseCode() {
        return courseCode;
    }

    /** Gets the name of the course object.
     *
     * @return A String containing the course name
     */
    public String getCourseName() {
        return courseName;
    }

    /** Sets the name of the course object.
     *
     * @param courseName String containing the course name to be set
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /** Sets the school that offers the course.
     *
     * @param school Enum value of a school in NTU
     * @see ValueObject.School
     */
    public void setSchool(School school) {
        this.school = school;
    }

    /** Sets the lecture venue of the course
     * @param lectureVenue venue of course lecture
     * @see ValueObject.Venue
     */
    public void setLectureVenue(Venue lectureVenue) {
        this.lectureVenue = lectureVenue;
    }

    /** Gets the number of academic units (AUs) of the course.
     *
     * @return An Integer representing the number of AUs of the course
     */
    public int getAUs() {
        return AUs;
    }

    /** Gets the vacancies of an index object from the indexes TreeMap.
     * If the index object is null, throw a NonExistentIndexException error.
     *
     * @param indexNumber An integer that represents the index number of the index object to be checked for
     * @return vacancies of the index group specified
     * @throws NonExistentIndexException non existent index
     * @see Index#getVacancy()
     */
    public int checkVacancies(int indexNumber) throws NonExistentIndexException {
        Index index = indexes.get(indexNumber);
        if (index == null) {
            throw new NonExistentIndexException();
        } else {
            return index.getVacancy();
        }
    }

    /** Adds an index object to the indexes TreeMap.
     *
     * @param index An Index object to be added to the course
     */
    public void addIndex(Index index) {
        indexes.put(index.getIndexNumber(), index);
    }

    /** Deletes an index object from the indexes TreeMap.
     *
     * @param indexNumber An integer representing the index group number
     * @throws NonExistentIndexException Throws Exception when there is a non existent index
     */
    public void deleteIndex(int indexNumber) throws NonExistentIndexException {
        if (!indexes.containsKey(indexNumber)) {
            throw new NonExistentIndexException();
        } else {
            indexes.remove(indexNumber);
        }
    }

    /** Retrieves an index object from the indexes TreeMap.
     *
     * @param indexNumber An integer that represents the index number of the index object to be retrieved
     * @return Index object with the specified index number
     */
    public Index getIndex(int indexNumber) {
        return indexes.get(indexNumber);
    }

    /** Retrieves index numbers of the course.
     *
     * @return List containing strings of index numbers
     */
    public List<String> getListOfIndexNumbers(){
        List<String> indexList = new ArrayList<>();
        for (Integer indexNumber : indexes.keySet()) {
            indexList.add(indexNumber.toString());
        }
        return indexList;
    }

    /** Updates the information of an existing index object.
     * Replaces the existing Index object in the indexes TreeMap
     * with an Index object containing updated information on the index group.
     *
     * @param index object with update information
     */
    public void updateIndex(Index index) {
        indexes.replace(index.getIndexNumber(), index);
    }

    /** Determines if there is a clash in lecture timings between two courses.
     * Loops through the lecture days in the Lecture Timings TreeMap
     * to check if there is an overlap in lecture timings with another course on any particular day.
     *
     * @param c course to check against
     * @return A boolean that is true if there is a clash in lecture timings
     * @see #clashingTimetable(Hashtable, Hashtable, DayOfWeek)
     */
    public boolean isClashing(Course c) {
        for(DayOfWeek thisLectureDay: this.lectureTimings.keySet()) {
            if (clashingTimetable(lectureTimings, c.lectureTimings, thisLectureDay)) {
                return true;
            }
        }
        return false;
    }

    /** Determines if there is a clash in timings between the laboratory and tutorial sessions (if any) of a given index
     * and the lecture timings of the course.
     * Loops through the lecture days in the Lecture Timings TreeMap
     * to check if there is an overlap in timings with the laboratory or tutorial sessions
     * of a given index on any particular day.
     *
     * @param i index to check against
     * @return A boolean that is true if there is a clash in timings
     * @see #clashingTimetable(Hashtable, Hashtable, DayOfWeek)
     */
    public boolean isClashing(Index i) {
        Hashtable<DayOfWeek, List<LocalTime>> laboratoryTimings = i.getLaboratoryTimings();
        Hashtable<DayOfWeek, List<LocalTime>> tutorialTimings = i.getTutorialTimings();
        for (DayOfWeek lectureDay : lectureTimings.keySet()) {
            if (clashingTimetable(lectureTimings, laboratoryTimings, lectureDay) ||
                    clashingTimetable(lectureTimings, tutorialTimings, lectureDay)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if two time slots overlap.
     *
     * @param thisCourseTimings start time and end time of current index lecture/ laboratory/ tutorial
     * @param newCourseTimings start and end time of new index laboratory/ tutorial
     * @param thisCourseDay day of current course lecture/ laboratory/ tutorial
     * @return A boolean that is true if and only if there is a clash in timings.
     */
    private boolean clashingTimetable(Hashtable<DayOfWeek, List<LocalTime>> thisCourseTimings,
                                      Hashtable<DayOfWeek, List<LocalTime>> newCourseTimings,
                                      DayOfWeek thisCourseDay) {
        if (thisCourseDay == null || newCourseTimings == null || thisCourseTimings == null) {
            return false;
        }
        for (DayOfWeek thatLectureDay : newCourseTimings.keySet()) {
            if (thisCourseDay == thatLectureDay) {
                if (overlappingTimeslot(thisCourseTimings.get(thisCourseDay).get(0),
                        thisCourseTimings.get(thisCourseDay).get(1),
                        newCourseTimings.get(thatLectureDay).get(0),
                        newCourseTimings.get(thatLectureDay).get(1))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Determines if two timings overlap.
     * Timings overlap if a session starts before the other session ends and
     * if the session also ends after the other session starts.
     *
     * @param start1 LocalTime object that contains the start time of a session
     * @param end1 LocalTime object that contains the end time of a session
     * @param start2 LocalTime object that contains the start time of the other session
     * @param end2 LocalTime object that contains the end time of the other session
     * @return A boolean that is true if there is an overlap in two timings
     */
    private boolean overlappingTimeslot(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /** Builds a String containing all course information.
     * Loops through the indexes TreeMap to get index numbers of all index groups.
     *
     * @return A String containing the course code, course name, school, lecture timings and venue and index numbers
     * @see ValueObject.School
     * @see ValueObject.Venue
     */
    public String allInfoToString() {
        StringBuilder str = new StringBuilder();
        str.append("---------------latest course info---------------\n").append(toString());
        str.append("\nindex group numbers: ");
        int i = 1;
        for (Index index : indexes.values()) {
            str.append('\n').append(i++).append(") ").append(index.getIndexNumber());
        }
        return str.toString();
    }

    /** Builds a String containing basic course information.
     *
     * @return A String containing the course code, course name and school
     * @see ValueObject.School
     */
    @Override
    public String toString() {
        return "courseCode : " + courseCode + "\tcourseName: " + courseName + "\tschool: " + school +
                "\nlecture timings: " + lectureTimings + "\tlecture venue: " + lectureVenue;
    }

}