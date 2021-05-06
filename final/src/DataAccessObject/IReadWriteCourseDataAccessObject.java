package DataAccessObject;

import EntityObject.Course;
import Exception.ExistingCourseException;
import Exception.NonExistentCourseException;

/**
 * Interface of read and write CourseDataAccessObject refer to {@link DataAccessObject.IReadCourseDataAccessObject} for read methods
 */
public interface IReadWriteCourseDataAccessObject extends IReadCourseDataAccessObject {
    /**
     * Add a new course
     * @param newCourse A Course object
     * @throws ExistingCourseException course already exist
     */
    void addCourse(Course newCourse) throws ExistingCourseException;

    /**
     * Add an existing course
     * @param course A Course object
     * @throws NonExistentCourseException course does not exist
     */
    void deleteCourse(Course course) throws NonExistentCourseException;

    /**
     * Add a new course
     * @param newCourse A Course object
     */
    void updateCourse(Course newCourse);
}
