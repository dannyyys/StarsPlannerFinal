package DataAccessObject;

import EntityObject.Staff;
import EntityObject.Student;
import Exception.ExistingUserException;
import Exception.NonExistentUserException;

import java.util.List;

/**
 * Interface of read and write UserDataAccessObject refer to {@link DataAccessObject.IReadUserDataAccessObject} for read methods
 */
public interface IReadWriteUserDataAccessObject extends IReadUserDataAccessObject {
    /**
     * Override existing student
     * @param student Student object
     * @throws NonExistentUserException student does not exist
     */
    void updateStudent(Student student) throws NonExistentUserException;

    /**
     * Add a new student
     * @param student Student object
     * @throws ExistingUserException student already exist
     */
    void addStudent(Student student) throws ExistingUserException;

    /**
     * Add a new staff
     * @param staff Staff object
     * @throws ExistingUserException Staff already exist
     */
    void addAdmin(Staff staff) throws ExistingUserException;

    /**
     * construct student info string
     * @return string containing all student's info
     */
    String studentsInfoToString();

    /**
     * get all student matric numbers as a list
     * @return list of student matric numbers
     */
    List<String> getAllStudentMatricNumbers();
}
