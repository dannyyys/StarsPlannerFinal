package HelperObject;

import ControlObject.*;
import DataAccessObject.*;
import EntityObject.*;
import ValueObject.*;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


public class Factory {
    /**
     * Creates a login control terminal.
     * @return LoginControl
     */
    public static ConsoleLoginControl createLoginControl() {
        TextIO textIO = TextIoFactory.getTextIO();
        return new ConsoleLoginControl(textIO, textIO.getTextTerminal());
    }

    /**
     * Gets the read and write/ read only version of TextUserDataAccessObject interface.
     * Admin session, Student course registrar, registration data access object returns read and write version
     * User session, login control returns read only version
     * @param a admin session
     * @return an instance of read and write/ read only version of TextUserDataAccessObject
     * @throws IOException when there is error in accessing files
     * @throws ClassNotFoundException when class is not found
     */
    public static IReadWriteUserDataAccessObject getTextUserDataAccessObject(ConsoleAdminSession a) throws IOException, ClassNotFoundException {
        return TextUserDataAccessObject.getInstance();
    }

    /**
     * @param u user session
     * Refer to {@link #getTextUserDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadUserDataAccessObject getTextUserDataAccessObject(ConsoleUserSession u) throws IOException, ClassNotFoundException {
        return TextUserDataAccessObject.getInstance();
    }

    /**
     * @param s student course registrar
     * Refer to {@link #getTextUserDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadWriteUserDataAccessObject getTextUserDataAccessObject(StudentCourseRegistrar s) throws IOException, ClassNotFoundException {
        return TextUserDataAccessObject.getInstance();
    }

    /**
     * @param r registration data access object
     * Refer to {@link #getTextUserDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadWriteUserDataAccessObject getTextUserDataAccessObject(IReadWriteRegistrationDataAccessObject r) throws IOException, ClassNotFoundException {
        return TextUserDataAccessObject.getInstance();
    }

    /**
     * @param l login control object
     * Refer to {@link #getTextUserDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadUserDataAccessObject getTextUserDataAccessObject(ILoginControl l) throws IOException, ClassNotFoundException {
        return TextUserDataAccessObject.getInstance();
    }

    /**
     * Gets the read and write/ read only version of TextCourseDataAccessObject interface.
     * Admin session, Student course registrar, registration data access object returns read and write version
     * User session returns read only version
     * @param a admin session
     * @return an instance of read and write/ read only version of TextCourseDataAccessObject
     * @throws IOException when there is error in accessing files
     * @throws ClassNotFoundException when class is not found
     */
    public static IReadWriteCourseDataAccessObject getTextCourseDataAccessObject(ConsoleAdminSession a) throws IOException, ClassNotFoundException {
        return TextCourseDataAccessObject.getInstance();
    }

    /**
     * @param u user session
     * Refer to {@link #getTextCourseDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadCourseDataAccessObject getTextCourseDataAccessObject(ConsoleUserSession u) throws IOException, ClassNotFoundException {
        return TextCourseDataAccessObject.getInstance();
    }

    /**
     * @param s student course registrar
     * Refer to {@link #getTextCourseDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadWriteCourseDataAccessObject getTextCourseDataAccessObject(StudentCourseRegistrar s) throws IOException, ClassNotFoundException {
        return TextCourseDataAccessObject.getInstance();
    }

    /**
     * @param r registration data access object
     * Refer to {@link #getTextCourseDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadWriteCourseDataAccessObject getTextCourseDataAccessObject(IReadWriteRegistrationDataAccessObject r) throws IOException, ClassNotFoundException {
        return TextCourseDataAccessObject.getInstance();
    }

    /**
     * Gets the read and write/ read only version of TextRegistrationDataAccessObject interface.
     * Admin session, Student course registrar returns read and write version
     * User session returns read only version
     * @param a admin session
     * @return an instance of read and write/ read only version of TextRegistrationDataAccessObject
     * @throws IOException when there is error in accessing files
     * @throws ClassNotFoundException when class is not found
     */
    public static IReadWriteRegistrationDataAccessObject getTextRegistrationDataAccessObject(ConsoleAdminSession a) throws IOException, ClassNotFoundException {
        return TextRegistrationDataAccessObject.getInstance();
    }

    /**
     * @param u user session
     * Refer to {@link #getTextRegistrationDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadRegistrationDataAccessObject getTextRegistrationDataAccessObject(ConsoleUserSession u) throws IOException, ClassNotFoundException {
        return TextRegistrationDataAccessObject.getInstance();
    }

    /**
     * @param s student course registrar
     * Refer to {@link #getTextRegistrationDataAccessObject(ConsoleAdminSession a)} for the overloaded method}.
     */
    public static IReadWriteRegistrationDataAccessObject getTextRegistrationDataAccessObject(StudentCourseRegistrar s) throws IOException, ClassNotFoundException {
        return TextRegistrationDataAccessObject.getInstance();
    }

    /**
     * Creates session appropriate for the user given.
     * @param user AbstractUser object given
     * @return appropriate session based on user type
     */
    public static ISession createSession(AbstractUser user) {
        TextIO textIO = TextIoFactory.getTextIO();
        switch (user.getUserType()) {
            case ADMIN -> {
                return new ConsoleAdminSession(textIO, textIO.getTextTerminal(), user);
            }
            case USER -> {
                return new ConsoleUserSession(textIO, textIO.getTextTerminal(), user);
            }
            default -> throw new NullPointerException();
        }
    }

    /**
     * Creates a Student with given maximum AUs value.
     * @param name String that stores the student's name
     * @param school School object that stores the school the student belongs to
     * @param gender Gender object that stores the student's gender
     * @param nationality Nationality object that stores the student's nationality
     * @param maxAUs Integer that stores the maximum AUs that student can take
     * @return Student object
     * @throws PasswordStorage.CannotPerformOperationException unsafe to verify password on platform
     */
    public static Student createStudent(String name, School school, Gender gender, Nationality nationality, int maxAUs) throws PasswordStorage.CannotPerformOperationException {
        return new Student(name, school, gender, nationality, maxAUs, new Random());
    }

    /**
     * Refer to {@link #createStudent(String, School, Gender, Nationality, int)} for the overloaded method}.
     */
    public static Student createStudent(String name, School school, Gender gender, Nationality nationality) throws PasswordStorage.CannotPerformOperationException {
        return new Student(name, school, gender, nationality, 21 , new Random());
    }

    /**
     * Creates a Staff (admin) person.
     * @param name String that stores the Staff person's name
     * @param school School object that stores the school the Staff person belongs to
     * @param gender Gender object that stores the Staff person's gender
     * @param nationality Nationality object that stores the Staff person's nationality
     * @return Staff object
     * @throws PasswordStorage.CannotPerformOperationException unsafe to verify password on platform
     */
    public static Staff createStaff(String name, School school, Gender gender, Nationality nationality, String job) throws PasswordStorage.CannotPerformOperationException {
        return new Staff(name, school, gender, nationality, job);
    }

    /**
     * Creates an index with tutorials and lab sessions.
     * @param indexNumber Integer that stores the index number of the index class
     * @param maxClassSize Integer that stores the maximum number of students allowed to be registered in the index class
     * @return Index object
     */
    public static Index createIndex(int indexNumber, int maxClassSize) {
        return new Index(indexNumber, maxClassSize, null, null, null, null);
    }

    /**
     * Creates a course, given indexes.
     * @param courseCode String that stores code of the course
     * @param courseName String that stores the name of the course
     * @param school School object that stores which school the course belongs to
     * @param lectureTimings Hashtable object that stores lecture timings as a day of the week, and time in HH:MM format
     * @param lectureVenue Venue object that stores lecture location of the course
     * @param AUs Integer that stores the course's AU value
     * @param indexes ArrayList object that stores indexes of the course
     * @return newly created course
     */
    public static Course createCourse(String courseCode, String courseName, School school, Hashtable<DayOfWeek, List<LocalTime>> lectureTimings, Venue lectureVenue, int AUs, ArrayList<Index> indexes) {
        return new Course(courseCode, courseName, school, lectureTimings, lectureVenue, AUs, indexes);
    }

    /**
     * @return newly created course
     * Refer to {@link #createCourse(String, String, School, Hashtable, Venue, int, ArrayList)}  for the overloaded method}.
     */
    public static Course createCourse(String courseCode, String courseName, School school, Hashtable<DayOfWeek, List<LocalTime>> lectureTimings, Venue lectureVenue, int AUs) {
        return createCourse(courseCode, courseName, school, lectureTimings, lectureVenue, AUs, new ArrayList<>());
    }

    /**
     * Creates a registration period.
     * @param startDate LocalDateTime object that stores start date of Registration Period
     * @param endDate LocalDateTime object that stores end date of Registration Period
     * @return RegistrationPeriod object
     */
    public static RegistrationPeriod createRegistrationPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return new RegistrationPeriod(startDate, endDate);
    }

    /**
     * Creates a registration key.
     * @param matricNumber String that stores matriculation number of student
     * @param courseCode String that stores code of the course
     * @param indexNumber Integer that stores index number of the course
     * @return RegistrationKey object
     */
    public static RegistrationKey createRegistrationKey(String matricNumber, String courseCode, int indexNumber) {
        return new RegistrationKey(matricNumber, courseCode, indexNumber);
    }

    /**
     * Creates email messenger.
     * @param recipientEmail String that stores email of recipient
     * @return EmailMessenger object
     */
    public static IMessenger createEmailMessenger(String recipientEmail) {
        return new EmailMessenger(recipientEmail);
    }

    /**
     * Creates a registration.
     * @return StudentCourseRegistrar object
     */
    public static StudentCourseRegistrar createStudentCourseRegistrar() {
        return new StudentCourseRegistrar();
    }
}