package ControlObject;

import DataAccessObject.IReadWriteCourseDataAccessObject;
import DataAccessObject.IReadWriteRegistrationDataAccessObject;
import DataAccessObject.IReadWriteUserDataAccessObject;
import HelperObject.Factory;
import EntityObject.Course;
import EntityObject.Index;
import ValueObject.RegistrationKey;
import Exception.*;
import EntityObject.Student;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

/**
 * control object to facilitate adding and dropping of courses.
 */
public class StudentCourseRegistrar {
    /**
     * Register student to a course index using information from registrationKey
     * @param matricNumber, student matric number.
     * @param courseCode course to be added.
     * @param indexNumber index number to be added.
     * @see HelperObject.Factory#getTextCourseDataAccessObject(ConsoleAdminSession)
     * @see DataAccessObject.IReadWriteCourseDataAccessObject#getCourse(String)
     *
     * @see HelperObject.Factory#getTextUserDataAccessObject(ConsoleAdminSession)
     * @see DataAccessObject.IReadWriteUserDataAccessObject#updateStudent(Student)
     *
     * @see ValueObject.RegistrationKey#getCourseCode()
     * @see ValueObject.RegistrationKey#getIndexNumber()
     * @see ValueObject.RegistrationKey#getMatricNumber()
     *
     * @see EntityObject.Course#getIndex(int)
     *
     * @see EntityObject.Index#enrollStudent(String)
     *
     * @see EntityObject.Student#registerWaitListCourse(String, int)
     * @see EntityObject.Student#registerCourse(String, int)
     * @see EntityObject.Student#registerAUs(int)
     *
     * @throws IOException cannot find file
     * @throws ClassNotFoundException class is not defined in project
     * @throws InvalidAccessPeriodException accessed outside registration period
     * @throws InsufficientAUsException not enough AUs to add course
     * @throws ExistingCourseException course already exists
     * @throws ExistingUserException  user already exists
     * @throws ExistingRegistrationException course already registered
     * @throws NonExistentUserException user does not exist
     * @throws MaxEnrolledStudentsException index reach it maximum student capacity
     * @throws ClashingTimeTableException index will result in clashing time table
     */
    public void addRegistration(String matricNumber, String courseCode, int indexNumber) throws IOException, ClassNotFoundException, InvalidAccessPeriodException, InsufficientAUsException, ExistingCourseException, ExistingUserException, ExistingRegistrationException, NonExistentUserException, MaxEnrolledStudentsException, ClashingTimeTableException {
        //create data access objects for user, course and registration
    	IReadWriteUserDataAccessObject userDataAccessObject = Factory.getTextUserDataAccessObject(this);
        IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
        IReadWriteRegistrationDataAccessObject registrationDataAccessObject =
                Factory.getTextRegistrationDataAccessObject(this);

        //checks if method is called during access period
        if (registrationDataAccessObject.getRegistrationPeriod().notWithinRegistrationPeriod()) {
            throw new InvalidAccessPeriodException();
        }

        //initialise variables to store information relevant to registration key
        RegistrationKey registrationKey = Factory.createRegistrationKey(matricNumber, courseCode, indexNumber);
        Student student = userDataAccessObject.getStudent(matricNumber);
        Course course = courseDataAccessObject.getCourse(courseCode);
        Index index = course.getIndex(indexNumber);

        //checks if new course added will exceed student's AU limit
        if (student.getMaxAUs() - student.getTotalRegisteredAUs() < course.getAUs()) {
            throw new InsufficientAUsException();
        }

        //variables to store student's registered courses and waitlisted courses
        Set<String> registeredCourses = student.getRegisteredCourses().keySet();
        Set<String> waitlist = student.getWaitingListCourses().keySet();
        ArrayList<String> allRegisteredCourses = new ArrayList<>();
        allRegisteredCourses.addAll(registeredCourses);
        allRegisteredCourses.addAll(waitlist);

        //checks through student's current registered courses for compatibility
        for (String registeredCourseCode : allRegisteredCourses) {
            int registeredCourseIndexNumber;
            if (student.getRegisteredCourses() == null || !student.getRegisteredCourses().containsKey(registeredCourseCode)) {
                registeredCourseIndexNumber = student.getWaitingListCourses().get(registeredCourseCode);
            } else {
                registeredCourseIndexNumber = student.getRegisteredCourses().get(registeredCourseCode);
            }
            Course registeredCourse = courseDataAccessObject.getCourse(registeredCourseCode);
            Index registeredIndex = registeredCourse.getIndex(registeredCourseIndexNumber);

            //checks if course being registered would clash with student's current time table
            if (registeredCourse.isClashing(course) || registeredCourse.isClashing(index) ||
                    registeredIndex.isClashing(index)) {
                throw new ClashingTimeTableException();
            }
        }

        registrationDataAccessObject.addRegistration(registrationKey); //successfully registers course
    }

    /**
     * Delete a student from a course index using information from registrationKey
     * @param matricNumber, student matric number.
     * @param courseCode course to be added.
     * @param indexNumber index number to be added.
     * @see HelperObject.Factory#getTextCourseDataAccessObject(ConsoleAdminSession)
     * @see DataAccessObject.IReadWriteCourseDataAccessObject#getCourse(String)
     * @see DataAccessObject.IReadWriteCourseDataAccessObject#updateCourse(Course)
     *
     * @see HelperObject.Factory#getTextUserDataAccessObject(ConsoleAdminSession)
     * @see DataAccessObject.IReadWriteUserDataAccessObject#updateStudent(Student)
     * @see DataAccessObject.IReadWriteUserDataAccessObject#getStudent(String)
     * @see EntityObject.Student#getEmail()
     *
     * @see ValueObject.RegistrationKey#getCourseCode()
     * @see ValueObject.RegistrationKey#getIndexNumber()
     * @see ValueObject.RegistrationKey#getMatricNumber()
     * @see HelperObject.Factory#createRegistrationKey(String, String, int)
     *
     * @see EntityObject.Course#getIndex(int)
     * @see EntityObject.Course#updateIndex(Index)
     *
     * @see EntityObject.Index#dropStudent(String)
     *
     * @see DataAccessObject.IReadWriteUserDataAccessObject#getStudent(String)
     * @see EntityObject.Student#deregisterCourse(String)
     * @see EntityObject.Student#deregisterAUs(int)
     *
     * @see HelperObject.Factory#createEmailMessenger(String)
     * @see HelperObject.IMessenger#addRecipientEmail(String)
     * @see HelperObject.IMessenger#sendMessage(String, String)
     *
     * @throws NonExistentRegistrationException student have not registered for the course index
     * @throws IOException cannot find file
     * @throws ClassNotFoundException class is not defined in project
     * @throws InvalidAccessPeriodException accessed outside registration period
     * @throws NonExistentRegistrationException index not registered
     * @throws NonExistentUserException user does not exist
     * @throws NonExistentCourseException course does not exist
     * @throws ExistingCourseException course already exists
     * @throws MaxEnrolledStudentsException index reach it maximum student capacity
     * @throws ExistingUserException user already exist
     */
    public void deleteRegistration(String matricNumber, String courseCode, int indexNumber) throws IOException, ClassNotFoundException, InvalidAccessPeriodException, NonExistentRegistrationException, NonExistentUserException, NonExistentCourseException, ExistingCourseException, MaxEnrolledStudentsException, ExistingUserException {
        //create data access objects for course and registration
    	IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
        IReadWriteRegistrationDataAccessObject registrationDataAccessObject =
                Factory.getTextRegistrationDataAccessObject(this);

        //checks if method is called during access period
        if (registrationDataAccessObject.getRegistrationPeriod().notWithinRegistrationPeriod()) {
            throw new InvalidAccessPeriodException();
        }

        //initialise variables to store information relevant to registration key
        RegistrationKey registrationKey = Factory.createRegistrationKey(matricNumber, courseCode, indexNumber);
        Course course = courseDataAccessObject.getCourse(courseCode);
        Index index = course.getIndex(indexNumber);

        //drops course even when student is in waiting list for it
        if (index.getWaitingList().contains(matricNumber)) {
            index.dropStudent(matricNumber);
            course.updateIndex(index);
            courseDataAccessObject.updateCourse(course);

            IReadWriteUserDataAccessObject userDataAccessObject = Factory.getTextUserDataAccessObject(this);
            Student student = userDataAccessObject.getStudent(matricNumber);
            student.deregisterCourse(courseCode);
            userDataAccessObject.updateStudent(student);
        } else {
            registrationDataAccessObject.deleteRegistration(registrationKey); //successfully removes registered course
        }
    }
}
