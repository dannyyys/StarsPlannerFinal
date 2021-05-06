package DataAccessObject;

import ValueObject.RegistrationKey;
import ValueObject.RegistrationPeriod;
import Exception.*;

import java.io.IOException;

/**
 * Interface of read and write RegistrationDataAccessObject refer to {@link DataAccessObject.IReadRegistrationDataAccessObject} for read methods
 */
public interface IReadWriteRegistrationDataAccessObject extends IReadRegistrationDataAccessObject {
    /**
     * Overrides existing registration period
     * @param newRegistrationPeriod RegistrationPeriod object that contains the start and end time
     * @throws IdenticalRegistrationPeriodException New registration period same as old one
     */
    void updateRegistrationPeriod(RegistrationPeriod newRegistrationPeriod) throws IdenticalRegistrationPeriodException;

    /**
     * Register student to a course index using information from registrationKey
     * @param registrationKey RegistrationKey object that contains student matric number, course code and index
     * @throws ExistingRegistrationException existing registration
     * @throws IOException cannot find file
     * @throws ClassNotFoundException class is not defined in project
     * @throws ExistingCourseException course already exists
     * @throws ExistingUserException  user already exists
     * @throws NonExistentUserException user does not exist
     * @throws MaxEnrolledStudentsException index reach it maximum student capacity
     */
    void addRegistration(RegistrationKey registrationKey) throws ExistingRegistrationException, IOException, ClassNotFoundException, ExistingCourseException, ExistingUserException, NonExistentUserException, MaxEnrolledStudentsException;

    /**
     * Delete a student from a course index using information from registrationKey
     * @param registrationKey RegistrationPeriod object that contains student matric number, course code and index
     * @throws NonExistentRegistrationException student have not registered for the course index
     * @throws IOException cannot find file
     * @throws ClassNotFoundException class is not defined in project
     * @throws NonExistentUserException user does not exist
     * @throws NonExistentCourseException course does not exist
     * @throws ExistingCourseException course already exists
     * @throws MaxEnrolledStudentsException index reach it maximum student capacity
     * @throws ExistingUserException user already exist
     */
    void deleteRegistration(RegistrationKey registrationKey) throws NonExistentRegistrationException, IOException, ClassNotFoundException, NonExistentUserException, NonExistentCourseException, ExistingCourseException, MaxEnrolledStudentsException, ExistingUserException;
}