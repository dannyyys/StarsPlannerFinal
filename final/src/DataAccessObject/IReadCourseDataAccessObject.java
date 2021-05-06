package DataAccessObject;

import EntityObject.Course;

import java.util.List;

/**
 * Interface of read only CourseDataAccessObject
 */
public interface IReadCourseDataAccessObject {
    /**
     * Gets a course object
     * @param courseCode String that represents course code
     * @return A course object
     */
    Course getCourse(String courseCode);

    /**
     * Gets a List of course codes
     * @return A List of course codes
     */
    List<String> getAllCourseCodes();
}
