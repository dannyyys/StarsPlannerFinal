package DataAccessObject;

import ControlObject.ConsoleAdminSession;
import EntityObject.Course;
import EntityObject.Index;
import EntityObject.Student;
import Exception.*;
import HelperObject.Factory;
import HelperObject.IMessenger;
import ValueObject.*;

import java.io.*;
import java.util.Date;
import java.util.TreeMap;

/**
 * text version of registration data access object
 */
public class TextRegistrationDataAccessObject implements Serializable, IReadWriteRegistrationDataAccessObject {
    /**
     * TreeMap of Registrations done by students
     * Contains RegistrationKey object and DateTime when the registration was done in Long
     */
    private final TreeMap<RegistrationKey, Long> registrations = new TreeMap<>();
    /**
     * A null instance of RegistrationPeriod
     */
    private RegistrationPeriod registrationPeriod = null;
    /**
     * A null instance of TextRegistrationDataAccess
     */
    private static TextRegistrationDataAccessObject instance = null;

    /**
     * A private Constructor that prevents any other class from instantiating
     *
     */
    private TextRegistrationDataAccessObject() {
        super();
    }

    /**
     * Static 'instance' method
     * @return An instance of this class
     * @throws IOException cannot find file
     * @throws ClassNotFoundException Class does not exist
     */
    public static TextRegistrationDataAccessObject getInstance() throws IOException, ClassNotFoundException{
        initialize();
        if(instance == null){
            instance = new TextRegistrationDataAccessObject();
        }
        return instance;
    }

    /**
     * Reads in serialized Registrations database file
     * @throws IOException cannot find file
     * @throws ClassNotFoundException Class does not exist
     */
    private static void initialize() throws IOException, ClassNotFoundException {
        InputStream file = new FileInputStream("./data/Registrations.ser");
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        instance = (TextRegistrationDataAccessObject) input.readObject();
    }

    /**
     * Writes/update into serialized Registrations database file
     *
     */
    private static void persist(){
        FileOutputStream fos;
        ObjectOutputStream out = null;
        try{
            fos = new FileOutputStream("./data/Registrations.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(instance);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets registration period
     * @return RegistrationPeriod object that contains the start and end time
     */
    @Override
    public RegistrationPeriod getRegistrationPeriod() {
        return registrationPeriod;
    }

    /**
     * Overrides existing registration period
     * @param newRegistrationPeriod RegistrationPeriod object that contains the start and end time
     * @throws IdenticalRegistrationPeriodException New registration period same as old one
     */
    @Override
    public void updateRegistrationPeriod(RegistrationPeriod newRegistrationPeriod) throws IdenticalRegistrationPeriodException {
        if (registrationPeriod == null || !registrationPeriod.equals(newRegistrationPeriod)) {
            registrationPeriod = newRegistrationPeriod;
        } else {
            throw new IdenticalRegistrationPeriodException();
        }
    }

    /**
     /**
     * Register student to a course index using information from registrationKey
     * @param registrationKey RegistrationKey object that contains student matric number, course code and index
     *
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
     * @throws ExistingCourseException course already exists
     * @throws ExistingUserException  user already exists
     * @throws NonExistentUserException user does not exist
     * @throws MaxEnrolledStudentsException index reach it maximum student capacity
     */
    @Override
    public void addRegistration(RegistrationKey registrationKey) throws IOException, ClassNotFoundException, ExistingCourseException, ExistingUserException, NonExistentUserException, MaxEnrolledStudentsException {
        registrations.put(registrationKey, new Date().getTime());
        persist();

        //enroll student
        IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
        Course course = courseDataAccessObject.getCourse(registrationKey.getCourseCode());
        Index index = course.getIndex(registrationKey.getIndexNumber());
        String waitingListStudent =  index.enrollStudent(registrationKey.getMatricNumber());

        course.updateIndex(index);
        courseDataAccessObject.updateCourse(course);
        IReadWriteUserDataAccessObject userDataAccess = Factory.getTextUserDataAccessObject(this);
        Student student = userDataAccess.getStudent(registrationKey.getMatricNumber());

        if (waitingListStudent != null) {

            //update student info
            student.registerWaitListCourse(registrationKey.getCourseCode(), registrationKey.getIndexNumber());
            userDataAccess.updateStudent(student);
            throw new MaxEnrolledStudentsException();
        } else {

            //update student info
            student.registerCourse(registrationKey.getCourseCode(), registrationKey.getIndexNumber());
            student.registerAUs(course.getAUs());
            userDataAccess.updateStudent(student);
        }
    }

    /**
     * Delete a student from a course index using information from registrationKey
     * @param registrationKey RegistrationPeriod object that contains student matric number, course code and index
     * @see HelperObject.Factory#getTextCourseDataAccessObject(ConsoleAdminSession)
     * @see DataAccessObject.IReadWriteCourseDataAccessObject#getCourse(String)
     * @see DataAccessObject.IReadWriteCourseDataAccessObject#updateCourse(Course)
     *
     * @see HelperObject.Factory#getTextUserDataAccessObject(ConsoleAdminSession)
     * @see DataAccessObject.IReadWriteUserDataAccessObject#updateStudent(Student)
     * @see DataAccessObject.IReadWriteUserDataAccessObject#getStudent(String)
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
     * @see EntityObject.Student#deregisterCourse(String)
     * @see EntityObject.Student#deregisterAUs(int)
     * @see EntityObject.Student#getEmail()
     *
     * @see HelperObject.Factory#createEmailMessenger(String)
     * @see HelperObject.IMessenger#addRecipientEmail(String)
     * @see HelperObject.IMessenger#sendMessage(String, String)
     *
     * @throws IOException cannot find file
     * @throws ClassNotFoundException class is not defined in project
     * @throws NonExistentUserException user does not exist
     * @throws NonExistentCourseException course does not exist
     * @throws ExistingCourseException course already exists
     * @throws MaxEnrolledStudentsException index reach it maximum student capacity
     * @throws ExistingUserException user already exist
     */
    @Override
    public void deleteRegistration(RegistrationKey registrationKey) throws IOException, ClassNotFoundException, NonExistentUserException, NonExistentCourseException, ExistingCourseException, MaxEnrolledStudentsException, ExistingUserException {
        registrations.remove(registrationKey);

        IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
        Course course = courseDataAccessObject.getCourse(registrationKey.getCourseCode());
        Index index = course.getIndex(registrationKey.getIndexNumber());
        String waitingListStudentMatricNumber = index.dropStudent(registrationKey.getMatricNumber());
        course.updateIndex(index);
        courseDataAccessObject.updateCourse(course);

        //update student
        IReadWriteUserDataAccessObject userDataAccessObject = Factory.getTextUserDataAccessObject(this);
        Student student = userDataAccessObject.getStudent(registrationKey.getMatricNumber());
        student.deregisterCourse(registrationKey.getCourseCode());
        student.deregisterAUs(course.getAUs());
        userDataAccessObject.updateStudent(student);

        //add course for waiting list student
        if (waitingListStudentMatricNumber != null) {
            RegistrationKey newRegistrationKey = Factory.createRegistrationKey(waitingListStudentMatricNumber,
                    registrationKey.getCourseCode(),
                    registrationKey.getIndexNumber());
            addRegistration(newRegistrationKey);

            String waitingListStudentEmail = userDataAccessObject.getStudent(waitingListStudentMatricNumber).getEmail();
            IMessenger messenger = Factory.createEmailMessenger(waitingListStudentEmail);
            messenger.sendMessage("Course registered",
                    "Waiting list course " + course.getCourseCode() + ' ' +  course.getCourseName() + " index: "
                            + index.getIndexNumber() + " successfully added.\nPlease log in to check your STARS");
        }
    }
}
