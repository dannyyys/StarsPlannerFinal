package DataAccessObject;

import Exception.ExistingCourseException;
import Exception.NonExistentCourseException;
import EntityObject.Course;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * text version of course data access object
 */
public class TextCourseDataAccessObject implements Serializable, IReadWriteCourseDataAccessObject {
    /**
     * TreeMap of courses in NTU
     * Contains course code in String and Course object
     */
    private final TreeMap<String, Course> courses = new TreeMap<>();
    /**
     * A null instance of TextCourseDataAccess
     */
    private static TextCourseDataAccessObject instance = null;

    /**
     * A private Constructor that prevents any other class from instantiating
     *
     */
    private TextCourseDataAccessObject() {
        super();
    }

    /**
     * Static 'instance' method
     * @return An instance of this class
     * @throws IOException cannot find file
     * @throws ClassNotFoundException Class does not exist
     */
    public static TextCourseDataAccessObject getInstance() throws IOException, ClassNotFoundException{
        initialize();
        if(instance == null){
            instance = new TextCourseDataAccessObject();
        }
        return instance;
    }

    /**
     * Reads in serialized courses database file
     * @throws IOException cannot find file
     * @throws ClassNotFoundException Class does not exist
     */
    private static void initialize() throws IOException, ClassNotFoundException {
        InputStream file = new FileInputStream("./data/Courses.ser");
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        instance = (TextCourseDataAccessObject) input.readObject();
    }

    /**
     * Writes/update into serialized courses database file
     *
     */
    private static void persist(){
        FileOutputStream fos;
        ObjectOutputStream out = null;
        try{
            fos = new FileOutputStream("./data/Courses.ser");
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
     * Adds a new Course object
     * @param newCourse Course object
     */
    @Override
    public void addCourse(Course newCourse) throws ExistingCourseException {
        if (courses.containsKey(newCourse.getCourseCode())) {
            throw new ExistingCourseException();
        } else {
            courses.put(newCourse.getCourseCode(), newCourse);
            persist();
        }
    }

    /**
     * Delete an existing Course object
     * @param course Course object
     */
    @Override
    public void deleteCourse(Course course) throws NonExistentCourseException {
        if (!courses.containsKey(course.getCourseCode())) {
            throw new NonExistentCourseException();
        } else {
            courses.remove(course.getCourseCode(), course);
            persist();
        }
    }

    /**
     * Overrides an existing Course object
     * @param newCourse Course object
     */
    @Override
    public void updateCourse(Course newCourse) {
        courses.replace(newCourse.getCourseCode(), newCourse);
        persist();
    }

    /**
     * Gets Course object
     * @param courseCode String that represents course code
     * @return Course object with the corresponding course code
     */
    @Override
    public Course getCourse(String courseCode) {
        return courses.get(courseCode.toLowerCase());
    }

    /**
     * Gets course codes of existing courses in a List
     * @return A List as described above
     */
    @Override
    public List<String> getAllCourseCodes() {
        return new ArrayList<>(courses.keySet());
    }

    /**
     * Append all attributes of existing courses to a String
     * @return A String as described above
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("_______All Available Courses_______").append('\n');
        for (Course course : courses.values()) {
            str.append(course.toString()).append('\n');
        }
        return str.toString();
    }
}
