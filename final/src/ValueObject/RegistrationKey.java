package ValueObject;

import java.io.Serializable;

/**
 * Object that contains student matric number, course code and index number of the course
 * Helps to register or deregister student from course index
 */
public class RegistrationKey implements Comparable<RegistrationKey>, Serializable {
    /**
     * Student matric number
     */
    private final String matricNumber;
    /**
     * course code
     */
    private final String courseCode;
    /**
     * index number of a course
     */
    private final int indexNumber;
    /**
     * Serialized ID that is tagged to the registrationKey object
     */
    private static final long serialVersionUID = 1L;

    /**
     * Initialize all attributes of this class
     * @param matricNumber String that presents student's matric number
     * @param courseCode String containing the course code
     * @param indexNumber Integer that represents the index number
     */
    public RegistrationKey(String matricNumber, String courseCode, int indexNumber) {
        this.matricNumber = matricNumber;
        this.courseCode = courseCode;
        this.indexNumber = indexNumber;
    }

    /**
     * Gets a String that represents the student's matric number
     * @return String that presents student's matric number
     */
    public String getMatricNumber() {
        return matricNumber;
    }

    /** Gets the course code of the course object
     *
     * @return A String containing the course code
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * Gets index number of the index
     * @return Integer that represents the index number
     */
    public int getIndexNumber() {
        return indexNumber;
    }

    /**
     * Compares RegistrationKey object to another RegistrationKey object
     * @param other A RegistrationKey object
     * @return 1 if they are the same. 0 if they are not
     */
    @Override
    public int compareTo(RegistrationKey other) {
        if (this.matricNumber.equals(other.matricNumber)) {
            if (this.courseCode.equals(other.courseCode)) {
                return 0;
            } else {
                return this.courseCode.compareTo(other.courseCode);
            }
        } else {
            return this.matricNumber.compareTo(other.matricNumber);
        }
    }
}
