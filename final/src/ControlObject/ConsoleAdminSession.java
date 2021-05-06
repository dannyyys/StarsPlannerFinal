package ControlObject;

import DataAccessObject.IReadWriteCourseDataAccessObject;
import DataAccessObject.IReadWriteRegistrationDataAccessObject;
import DataAccessObject.IReadWriteUserDataAccessObject;
import EntityObject.*;
import HelperObject.EmailMessenger;
import HelperObject.Factory;
import HelperObject.InputValidator;
import HelperObject.PasswordStorage;
import ValueObject.*;
import Exception.*;
import org.beryx.textio.*;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Class for implementing admin session
 * Admin user can perform 9 functions: edit student access period,
 * 		add a student, add/update,delete course, check available slot for index number
 * 		print student list by index number, print student list by course,
 * 		update student's courses, log out and Exit.
 */
public class ConsoleAdminSession implements ISession {
    /**
     * TextIO console
     */
    private final TextIO _textIO;
    /**
     * textIO terminal
     */
    private final TextTerminal _terminal;
    /**
     * Boolean value that shows if student is logged in
     * Initialized as true
     */
    private boolean loggedIn = true;
    /**
     * Staff object
     */
    private final Staff _user;
    /**
     * String that contains message used for dashboard
     */
    private final String admin =
            "    _        _           _            ___                _      _                            _ \n" +
                    "   /_\\    __| |  _ __   (_)  _ _     |   \\   __ _   ___ | |_   | |__   ___   __ _   _ _   __| |\n" +
                    "  / _ \\  / _` | | '  \\  | | | ' \\    | |) | / _` | (_-< | ' \\  | '_ \\ / _ \\ / _` | | '_| / _` |\n" +
                    " /_/ \\_\\ \\__,_| |_|_|_| |_| |_||_|   |___/  \\__,_| /__/ |_||_| |_.__/ \\___/ \\__,_| |_|   \\__,_|\n" +
                    "                                                                                               ";
    /**
     * String that contains menu of admin session
     */
    private final String adminOptions =
                    "1. Edit student access period\n" +
                    "2. Add a student (name, matric number, gender, nationality, etc)\n" +
                    "3. Add/Update/Delete a course (course code, school, its index numbers and vacancy)\n" +
                    "4. Check available slot for an index number (vacancy in a class)\n" +
                    "5. Print student list by index number\n" +
                    "6. Print student list by course (all students registered for the selected course)\n" +
                    "7. Update student's courses/ Sign in as student\n" +
                    "8. Log out\n" +
                    "9. Exit\n";

    /**
     * Creates the Admin Session.
     * Initalises _textIO, _terminal and _user.
     *
     * @param textIO The TextIO Object
     * @param terminal The TextTerminal Object
     * @param user The Staff user
     */
    public ConsoleAdminSession(TextIO textIO, TextTerminal terminal, AbstractUser user) {
        _textIO = textIO;
        _terminal = terminal;
        _user = (Staff) user;
    }

    /**
     * Admin user logs out
     * @see org.beryx.textio.TextTerminal#resetToBookmark(String)
     */
    @Override
    public boolean logout() {
        _terminal.resetToBookmark("clear");
        return loggedIn;
    }

    /**
     * Exits the system.
     */
    @Override
    public void exit() {
        System.exit(0);
    }

    /**
     * Runs the program.
     */
    @Override
    public void run() {
        String keyStrokeAbort = "alt Z";
        boolean registeredAbort = _terminal.registerHandler(keyStrokeAbort,
                t -> new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT));

        int choice = 0;
        _terminal.getProperties().setPromptBold(true);
        _terminal.resetToBookmark("clear");
        _terminal.println(admin);
        //allows user to escape from a function at any given time by pressing alt Z
        if (registeredAbort) {
            _terminal.println("--------------------------------------------------------------------------------");
            _terminal.println("Press " + keyStrokeAbort + " to go abort your current action");
            _terminal.println("You can use this key combinations at any moment during your session.");
            _terminal.println("--------------------------------------------------------------------------------");
        }
        _terminal.setBookmark("admin");
        _terminal.println(adminOptions);
        _terminal.println("\t\twelcome " + _user.getName());

        //displays menu options and directs users to a selected functionality
        //users directed back to menu once the functionality has been completed
        do {
            try {
                _terminal.resetToBookmark("admin");
                _terminal.println(adminOptions);
                _terminal.println("\t\twelcome " + _user.getName());
                choice = _textIO.newIntInputReader()
                        .withMinVal(1).withMaxVal(9)
                        .read("Enter your choice: ");
                _terminal.resetToBookmark("admin");
                switch (choice) {
                    case 1 -> changeAccessPeriodMenu();
                    case 2 -> addStudentMenu();
                    case 3 -> addUpdateDropCourseMenu();
                    case 4 -> checkIndexVacanciesMenu();
                    case 5 -> printStudentListByIndexMenu();
                    case 6 -> printStudentListByCourseMenu();
                    case 7 -> {
                        loginAsStudentMenu();
                        if (registeredAbort) {
                            _terminal.println("--------------------------------------------------------------------------------");
                            _terminal.println("Press " + keyStrokeAbort + " to go abort your current action");
                            _terminal.println("You can use this key combinations at any moment during your session.");
                            _terminal.println("--------------------------------------------------------------------------------");
                        }
                    }
                    case 8 -> loggedIn = false;
                    case 9 -> exit();
                }
            } catch (ReadAbortedException ignored) {
            }
        } while (choice >= 0 && choice < 8);
    }

    /**
     * Admin user changes the student access period.
     * If input date format not correct, error message is printed and user prompted to enter period again
     * If input start period is later than end period, error message is printed and user prompted to enter period again
     * If input start period is same as end period, error message is printed and user prompted to enter period again
     * @see HelperObject.InputValidator#validateDateTimeInput(String)
     * @see HelperObject.Factory#createRegistrationPeriod(LocalDateTime, LocalDateTime)
     * @see HelperObject.Factory#getTextRegistrationDataAccessObject(ConsoleAdminSession)
     * @see DataAccessObject.IReadWriteRegistrationDataAccessObject#updateRegistrationPeriod(RegistrationPeriod)
     * @see org.beryx.textio.InputReader
     * @see org.beryx.textio.TextTerminal
     * @see java.time.LocalDateTime#parse(CharSequence, DateTimeFormatter)
     */
    private void changeAccessPeriodMenu() {
        boolean validDateTime;
        LocalDateTime startDate, endDate;
        _terminal.println("update registration period");
        _terminal.setBookmark("update registration period home screen");

        //prompts user to re-input start and end dates so long as the inputs are invalid
        do {
            String startDateStr;
            _terminal.setBookmark("start date");
            do {
                startDateStr = _textIO.newStringInputReader().read("new start date in yyyy-MM-dd HH:mm format: ");
                validDateTime = InputValidator.validateDateTimeInput(startDateStr);
                if (!validDateTime) {
                    _terminal.resetToBookmark("start date");
                    _terminal.getProperties().setPromptColor("red");
                    _terminal.println("invalid date format");
                    _terminal.getProperties().setPromptColor("white");
                }
            } while (!validDateTime);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            startDate = LocalDateTime.parse(startDateStr, format);

            String endDateStr;
            _terminal.setBookmark("end date");
            do {
                endDateStr = _textIO.newStringInputReader().read("new end date in yyyy-MM-dd HH:mm format: ");
                validDateTime = InputValidator.validateDateTimeInput(endDateStr);
                if (!validDateTime) {
                    _terminal.resetToBookmark("end date");
                    _terminal.getProperties().setPromptColor("red");
                    _terminal.println("invalid date format");
                    _terminal.getProperties().setPromptColor("white");
                }
            } while (!validDateTime);
            endDate = LocalDateTime.parse(endDateStr, format);

            //validates that start date occurs before end date
            validDateTime = startDate.compareTo(endDate) < 0;
            if (startDate.compareTo(endDate) > 0) {
                _terminal.resetToBookmark("update registration period home screen");
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("Start period should occur after end period");
                _terminal.getProperties().setPromptColor("white");
            } else if (startDate.compareTo(endDate) == 0) {
                _terminal.resetToBookmark("update registration period home screen");
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("Start and end period cannot be equal");
                _terminal.getProperties().setPromptColor("white");
            }
        } while (!validDateTime);
        RegistrationPeriod newRP = Factory.createRegistrationPeriod(startDate, endDate);

        //prompts admin user to ask if he/she wishes to inform students about the changes in registration period
        try {
            IReadWriteRegistrationDataAccessObject registrationDataAccessObject =
                    Factory.getTextRegistrationDataAccessObject(this);
            registrationDataAccessObject.updateRegistrationPeriod(newRP);
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("successfully changed access period");
            boolean email = _textIO.newBooleanInputReader().
                    read("Do you want to inform all students by emailing them?");

            //if admin user wishes to email students, students' emails are added to the list of recipients
            if (email) {
                IReadWriteUserDataAccessObject userDataAccessObject = Factory.getTextUserDataAccessObject(this);
                List<String> allMatricNumbers = userDataAccessObject.getAllStudentMatricNumbers();

                _terminal.println("Please wait...");
                EmailMessenger emailMessenger = null;
                for (String matricNumber : allMatricNumbers) {
                    if (emailMessenger == null) {
                        emailMessenger = new EmailMessenger(userDataAccessObject.getStudent(matricNumber).getEmail());
                    } else {
                        emailMessenger.addRecipientEmail(userDataAccessObject.getStudent(matricNumber).getEmail());
                    }
                }
                if (emailMessenger == null) {
                    _terminal.getProperties().setPromptColor("red");
                    _terminal.println("There are no students to send notification to");
                } else {
                    emailMessenger.sendMessage("Change in registration period",
                            "Registration Period has changed to: " + newRP);
                    _terminal.println("Emails sent");
                }
            }
        } catch (IdenticalRegistrationPeriodException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("new registration period same as old registration period");
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error reading file");
        } finally {
            _terminal.getProperties().setPromptColor("white");
        }
        _textIO.newStringInputReader().withDefaultValue(" ").read("press enter to continue");
    }

    /**
     * Admin user adds a student by inputting required student information
     * If input for name contains numbers, error message is printed and user prompted to enter name again
     * If input for gender, nationality and school not in range or contains symbols,
     * 			error message is printed and user prompted to enter input again
     * If input for MaxAUs is out of range of 0-25, error message is printed and user prompted to enter MaxAUs again
     *
     * @see HelperObject.InputValidator#validateDateTimeInput(String)
     * @see HelperObject.Factory#getTextUserDataAccessObject(ConsoleAdminSession)
     * @see DataAccessObject.IReadWriteUserDataAccessObject#addStudent(Student)
     * @see DataAccessObject.IReadWriteUserDataAccessObject#studentsInfoToString()
     * @see HelperObject.InputValidator#validateNameInput(String)
     * @see org.beryx.textio.InputReader
     * @see org.beryx.textio.TextTerminal
     */
    private void addStudentMenu() {
        String name;
        boolean validName;
        _terminal.println("add student");
        _terminal.setBookmark("add student home screen");
        //continues prompting user to re-input student name so long as it has
        //less than 3 characters or contains numbers
        do {
            _terminal.setBookmark("student name");
            name = _textIO.newStringInputReader().withMinLength(3).read("name: ");
            validName = InputValidator.validateNameInput(name);
            if (!validName) {
                _terminal.resetToBookmark("student name");
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("name cannot contain number");
                _terminal.getProperties().setPromptColor("white");
            }
        } while (!validName);

        //Prompts user to select the student's gender, nationality and school
        Gender gender = _textIO.newEnumInputReader(Gender.class).read("Gender: ");
        Nationality nationality = _textIO.newEnumInputReader(Nationality.class).read("Nationality: ");
        School school = _textIO.newEnumInputReader(School.class).read("School: ");

        //prompts the user to input the max AUs so long as they give an input
        //less than 0 and greater than 25
        int maxAUs = _textIO.newIntInputReader()
                .withDefaultValue(21).withMinVal(0).withMaxVal(25)
                .read("MaxAUs: (leave blank for default 21 AUs)");
        try {
            Student newStudent = Factory.createStudent(name, school, gender, nationality, maxAUs);
            IReadWriteUserDataAccessObject userDataAccessObject = Factory.getTextUserDataAccessObject(this);
            userDataAccessObject.addStudent(newStudent);
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("successfully added student");
            _terminal.println("list of all students:");

            userDataAccessObject = Factory.getTextUserDataAccessObject(this);
            _terminal.println(userDataAccessObject.studentsInfoToString());
        } catch (ExistingUserException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("Student already exists");
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("Error reading file");
        } catch (PasswordStorage.CannotPerformOperationException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error hashing password");
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ").read("press enter to continue");
        }
    }

    /**
     * Admin user checks for index vacancies
     *
     * @see org.beryx.textio.InputReader
     * @see org.beryx.textio.TextTerminal
     */
    private void checkIndexVacanciesMenu() {
        try {
            _terminal.println("Check index vacancies");
            IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
            List<String> coursesString = courseDataAccessObject.getAllCourseCodes();
            String courseCode = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(coursesString)
                    .read("select course to check vacancies for: ");
            Course course = courseDataAccessObject.getCourse(courseCode);
            List<String> indexString = course.getListOfIndexNumbers();
            String indexNum = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(indexString)
                    .read("select index to check vacancies for: ");
            int vacancies = course.checkVacancies(Integer.parseInt(indexNum));

            //display the number of students in the waiting list (if any)
            //and vacancies of the index group
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("Successfully retrieved vacancies");
            if (vacancies <= 0) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.printf("There is 0/%d vacancy for %s of %s\n",
                        course.getIndex(Integer.parseInt(indexNum)).getMaxClassSize(),
                        indexNum, courseCode);
                if (vacancies >= -1) {
                    _terminal.println("There is " + -vacancies + " student in the waiting list");
                } else {
                    _terminal.println("There are " + -vacancies + " students in the waiting list");
                }
            } else {
                if (vacancies <= 1) {
                    _terminal.printf("There is %d/%d vacancy for %s of %s\n",
                            vacancies, course.getIndex(Integer.parseInt(indexNum)).getMaxClassSize(),
                            indexNum, courseCode);
                } else {
                    _terminal.printf("There are %d/%d vacancies for %s of %s\n",
                            vacancies, course.getIndex(Integer.parseInt(indexNum)).getMaxClassSize(),
                            indexNum, courseCode);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("file not found");
        } catch (NonExistentIndexException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("no such index");
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ").
                    read("press enter to continue");
        }
    }

    /**
     * Allows Admin to add, update or delete a course/course info.
     * <p>If admin user chooses to add course, prompts Admin user for a course. Proceeds to prompt user to
     * add course and index group details or update course page depending on whether course exists.
     *
     * <p>If Admin user chooses to update an existing course. Proceeds to update course page.
     * Update course page allows admin to update course details, add/update/delete index group and delete course.
     * If Admin chooses to add/update/delete index group, re-directs user to addUpdateDropIndexMenu().
     * If Admin chooses to delete course, all students in the course get de-registered.
     *
     *
     * @see #addUpdateDropIndexMenu(Course, int)
     */
    private void addUpdateDropCourseMenu() {
        String selectedCourseCode = null;
        Course selectedCourse = null;
        String courseName;
        School school;

        _terminal.println("add/update/delete course");
        _terminal.setBookmark("add/update course home page");

        //prompts user to select among a list of course codes, including option to add a new course
        try {
            IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
            List<String> coursesString = courseDataAccessObject.getAllCourseCodes();
            coursesString.add("add new course");
            selectedCourseCode = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(coursesString)
                    .read("select course to add/update/delete: ");
            selectedCourse = courseDataAccessObject.getCourse(selectedCourseCode);

            //selectedCourse will be null if admin user chooses to add a new course
            if (selectedCourse == null) {
                boolean validCourseCode;
                _terminal.setBookmark("add new course");
                do {
                    selectedCourseCode = _textIO.newStringInputReader().read("enter new course code");
                    _terminal.resetToBookmark("add/update course home page");
                    validCourseCode = InputValidator.courseStrMatcher(selectedCourseCode);
                    if (validCourseCode) {
                        selectedCourse = courseDataAccessObject.getCourse(selectedCourseCode);
                        if (selectedCourse != null) {
                            _terminal.getProperties().setPromptColor("red");
                            _terminal.println("Course already exist\nupdating course instead of adding...");
                            _terminal.getProperties().setPromptColor("white");
                            _terminal.setBookmark("add/update course home page");
                        }
                    } else {
                        _terminal.resetToBookmark("add new course");
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("invalid course code format");
                        _terminal.getProperties().setPromptColor("white");
                    }
                } while (!validCourseCode);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        //selectedCourse is null if a user selects among a course code or
        //inputs an existing course code while trying to add a new course
        if (selectedCourse == null) {
            Hashtable<DayOfWeek, List<LocalTime>> lectureTimings = new Hashtable<>();
            DayOfWeek lectureDay;
            List<LocalTime> lectureTiming;
            int AUs;
            _terminal.setBookmark("add/update course home page");
            _terminal.setBookmark("add course details");
            _terminal.println("____input the following details to add the course____");
            courseName = _textIO.newStringInputReader()
                    .withMinLength(3)
                    .read("course name: ");
            school = _textIO.newEnumInputReader(School.class).read("school: ");
            AUs = _textIO.newIntInputReader().withMinVal(1).withMaxVal(4)
                    .read("number of AUs (1-4): ");

            //prompts user to input a lecture session until they indicate that they wish
            //to stop adding lecture sessions
            boolean contAdd;
            _terminal.println("____add a day and time period for lecture session____");
            _terminal.setBookmark("add lecture session");
            do {
                lectureDay = _textIO.newEnumInputReader(DayOfWeek.class).
                        read("Enter lecture day: ");
                //validates that there is no other lecture session on the same day
                //if there is, user will be prompted to select another day
                if (lectureTimings.containsKey(lectureDay)) {
                    _terminal.resetToBookmark("add lecture session");
                    _terminal.getProperties().setPromptColor(Color.RED);
                    _terminal.println("there is already a lecture session on " + lectureDay +
                            ". please select another day");
                    _terminal.getProperties().setPromptColor(Color.WHITE);
                    contAdd = true;
                    continue;
                }
                lectureTiming = getValidTimeInput("lecture");
                lectureTimings.put(lectureDay, lectureTiming);
                contAdd = _textIO.newBooleanInputReader().
                        read("do you wish to continue adding a lecture session?");
            } while (contAdd);

            Venue lectureVenue = _textIO.newEnumInputReader(Venue.class)
                    .read("add the venue for the lecture session(s): ");

            _terminal.resetToBookmark("add course details");
            selectedCourse = Factory.createCourse(selectedCourseCode, courseName, school, lectureTimings, lectureVenue, AUs);
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("course details have been recorded.");
            _terminal.getProperties().setPromptColor("white");

            //index group number automatically generated by taking the last 4 digits of the course plus 2 extra digits
            //index group number incremented as admin user continues adding a new index
            String newIndexGroupsubStr = selectedCourseCode.substring(2);
            int newIndexGroup = Integer.parseInt(newIndexGroupsubStr) * 100;
            _terminal.setBookmark("add index info");
            do {
                _terminal.resetToBookmark("add index info");
                selectedCourse = addUpdateDropIndexMenu(selectedCourse, newIndexGroup);
                contAdd = _textIO.newBooleanInputReader().
                        read("Do you wish to continue adding another index?");
                if (contAdd) {
                    newIndexGroup++;
                }
            } while (contAdd);

            try {
                IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
                courseDataAccessObject.addCourse(selectedCourse);
                _terminal.getProperties().setPromptColor(Color.GREEN);
                _terminal.println("Successfully added course");
                List<String> courses = courseDataAccessObject.getAllCourseCodes();
                for (String courseCode : courses) {
                    _terminal.println(courseDataAccessObject.getCourse(courseCode).toString());
                }
            } catch (IOException | ClassNotFoundException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("file not found");
            } catch (ExistingCourseException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("existing course");
            } finally {
                _terminal.getProperties().setPromptColor("white");
            }
            _textIO.newStringInputReader()
                    .withDefaultValue(" ").
                    read("press enter to continue");
        } else {
            int option;
            do {
                //if the selected course does not have an index group, proceeds to delete course
                if (selectedCourse.getListOfIndexNumbers().size() == 0) {
                    _terminal.getProperties().setPromptColor("red");
                    _terminal.println("course has no more index, deleting course...");
                    _terminal.getProperties().setPromptColor("white");
                    option = 5;
                } else {
                    _terminal.resetToBookmark("add/update/delete course home page");
                    _terminal.getProperties().setPromptColor(Color.GREEN);
                    _terminal.println(selectedCourse.allInfoToString());
                    _terminal.getProperties().setPromptColor("white");
                    _terminal.println("________Select course info to add/update________\n" +
                            "1. Update course name\n" +
                            "2. Update school\n" +
                            "3. Update Lecture Venue\n" +
                            "4. Add/Update/Delete index group\n" +
                            "5. Delete course\n" +
                            "6. Exit function");

                    option = _textIO.newIntInputReader().
                            withMinVal(1).withMaxVal(6).
                            read("Enter your choice");
                }

                _terminal.setBookmark("update course details");

                switch (option) {
                    //---------------Update course name
                    case 1 -> {
                        String newCourseName = _textIO.newStringInputReader()
                                .read("enter new course name: ");
                        selectedCourse.setCourseName(newCourseName);
                        updateCourse(selectedCourse);
                        _terminal.getProperties().setPromptColor(Color.GREEN);
                        _terminal.println("Successfully updated course name");
                        _terminal.getProperties().setPromptColor("white");
                        _textIO.newStringInputReader().withDefaultValue(" ")
                                .read("press enter to continue");
                    }
                    //---------------Update school
                    case 2 -> {
                        school = _textIO.newEnumInputReader(School.class)
                                .read("enter new school: ");
                        selectedCourse.setSchool(school);
                        updateCourse(selectedCourse);
                        _terminal.getProperties().setPromptColor(Color.GREEN);
                        _terminal.println("Successfully updated school");
                        _terminal.getProperties().setPromptColor("white");
                        _textIO.newStringInputReader().withDefaultValue(" ")
                                .read("press enter to continue");
                    }
                    //---------------Update lecture venue
                    case 3 -> {
                        Venue lectureVenue = _textIO.newEnumInputReader(Venue.class)
                                .read("enter new venue for the lecture session(s): ");
                        selectedCourse.setLectureVenue(lectureVenue);
                        updateCourse(selectedCourse);
                        _terminal.getProperties().setPromptColor(Color.GREEN);
                        _terminal.println("Successfully updated lecture venue");
                        _terminal.getProperties().setPromptColor("white");
                        _textIO.newStringInputReader().withDefaultValue(" ").
                                read("press enter to continue");
                    }
                    //---------------Add/Update/Delete index group
                    case 4 -> {
                        //prompts user to select among index groups of courses plus addition of a new index group
                        List<String> indexesString = selectedCourse.getListOfIndexNumbers();
                        indexesString.add("add new index");
                        String indexInput = _textIO.newStringInputReader()
                                .withNumberedPossibleValues(indexesString).read("select index to add/update/delete:");
                        boolean validIndexNumber;
                        _terminal.resetToBookmark("add/update course home page");
                        _terminal.setBookmark("add new index number");

                        //if user chooses to add a new index group
                        //prompts user to re-input new index number so long as the format is invalid (not 6 digits)
                        if (!InputValidator.indexStrMatcher(indexInput)) {
                            do {
                                _terminal.setBookmark("add new index number");
                                indexInput = _textIO.newStringInputReader()
                                        .read("enter new index number: ");
                                validIndexNumber = InputValidator.indexStrMatcher(indexInput);
                                if (!validIndexNumber) {
                                    _terminal.resetToBookmark("add new index number");
                                    _terminal.getProperties().setPromptColor("red");
                                    _terminal.println("invalid course code format");
                                    _terminal.getProperties().setPromptColor("white");
                                }
                            } while (!validIndexNumber);
                        }
                        //calls for update of course containing latest information of index group
                        //after user goes through addUpdateDropIndexMenu
                        selectedCourse = addUpdateDropIndexMenu(selectedCourse, Integer.parseInt(indexInput));
                        updateCourse(selectedCourse);
                    }
                    //---------------Delete course
                    case 5 -> {
                        try {
                            IReadWriteRegistrationDataAccessObject registrationDataAccessObject =
                                    Factory.getTextRegistrationDataAccessObject(this);
                            List<String> indexNumbers = selectedCourse.getListOfIndexNumbers();
                            //Loops through all index groups to de-register students from deleted course
                            for (Iterator<String> indexNumberIterator = indexNumbers.iterator(); indexNumberIterator.hasNext();) {
                                String indexNumber = indexNumberIterator.next();
                                Index index = selectedCourse.getIndex(Integer.parseInt(indexNumber));
                                ArrayList<String> enrolledStudents = index.getEnrolledStudents();
                                Queue<String> waitingList = index.getWaitingList();
                                ArrayList<String> allStudents = new ArrayList<>();
                                allStudents.addAll(enrolledStudents);
                                allStudents.addAll(waitingList);

                                //loops through all enrolled students and students on waitlist for each index group
                                //and de-registers them from the course index
                                for (int i = 0; i < allStudents.size(); i++) {
                                    String student = allStudents.get(i);
                                    RegistrationKey registrationKey = Factory.createRegistrationKey(student,
                                            selectedCourseCode, Integer.parseInt(indexNumber));
                                    try {
                                        registrationDataAccessObject.deleteRegistration(registrationKey);
                                    } catch (IOException | ClassNotFoundException e) {
                                        _terminal.println("file not found");
                                    } catch (Exception e) {
                                        _terminal.println("Error deleting registration");
                                    }
                                }
                            }
                            //delete the course via the courseDataAccessObject
                            Factory.getTextCourseDataAccessObject(this).deleteCourse(selectedCourse);
                            _terminal.getProperties().setPromptColor(Color.green);
                            _terminal.println("Course deleted");
                        } catch (IOException | ClassNotFoundException e) {
                            _terminal.getProperties().setPromptColor("red");
                            _terminal.println("Error reading file");
                        } catch (NonExistentCourseException ignore) {
                        } finally {
                            _terminal.getProperties().setPromptColor("white");
                            _textIO.newStringInputReader().withDefaultValue(" ").
                                    read("press enter to continue");
                        }
                    }
                }
            } while (option != 5 && option != 6);
        }
    }

    /**
     * Allows Admin user to add, update or delete an index group.
     * Prompts user to select index group details to update, including class timings, venue and maximum class size.
     *
     * @param course A course object representing the course of the index group
     * @param index An integer representing the index number of the index group
     * @return The course object which the index group belongs to.
     */
    private Course addUpdateDropIndexMenu(Course course, int index) {
        Index existingIndex = course.getIndex(index);
        _terminal.setBookmark("add/update/delete index");
        //Add index if it does not exist
        if (existingIndex == null) {
            _terminal.println("____add details for new index group " + index
                    + " of course " + course.getCourseCode() + "____");
            int maxSize = _textIO.newIntInputReader()
                    .withMinVal(1)
                    .read("maximum class size of the index group: ");
            Index newIndex = Factory.createIndex(index, maxSize);
            course.addIndex(newIndex);
            existingIndex = newIndex;
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("Successfully added new index");
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ")
                    .read("press enter to continue");
            _terminal.resetToBookmark("add/update index");
        }
        int option;
        do {
            _terminal.resetToBookmark("add/update index");
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println(course.toString());
            _terminal.println(existingIndex.toString());
            _terminal.getProperties().setPromptColor("white");
            _terminal.println("____Select index info to add/update____\n" +
                    "1. Add/Update Tutorial Timing\n" +
                    "2. Add/Update Laboratory Timing\n" +
                    "3. Add/Update Tutorial Venue\n" +
                    "4. Add/Update Laboratory Venue\n" +
                    "5. Update Maximum Class Size\n" +
                    "6. Delete index\n" +
                    "7. Return to previous menu");
            option = _textIO.newIntInputReader()
                    .withMinVal(1).withMaxVal(7)
                    .read("Enter choice: ");

            switch (option) {
                case 1 -> {
                    Hashtable<DayOfWeek, List<LocalTime>> originalTutorialTimings = existingIndex.getTutorialTimings();
                    Hashtable<DayOfWeek, List<LocalTime>> newTutorialTimings;
                    _terminal.println("Add/Update Tutorial Timing");
                    newTutorialTimings = Objects.requireNonNullElseGet(originalTutorialTimings, Hashtable::new);
                    DayOfWeek sessionDay = _textIO.newEnumInputReader(DayOfWeek.class)
                            .read("Enter tutorial day: ");
                    List<LocalTime> sessionTiming = getValidTimeInput("tutorial");
                    newTutorialTimings.put(sessionDay, sessionTiming);

                    //Check if timing clashed with lecture timing
                    existingIndex.setTutorialTimings(newTutorialTimings);
                    if (course.isClashing(existingIndex)) {
                        existingIndex.setTutorialTimings(originalTutorialTimings);
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("Tutorial timing cannot clash with lecture timing");
                        _terminal.getProperties().setPromptColor("white");
                        _textIO.newStringInputReader().withDefaultValue(" ")
                                .read("press enter to continue");
                        break;
                    } else {
                        _terminal.getProperties().setPromptColor(Color.GREEN);
                        _terminal.println("Successfully added/updated tutorial timing");
                    }
                    _terminal.getProperties().setPromptColor("white");

                    if (existingIndex.getTutorialVenue() == null) {
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("Tutorial timing cannot exist without venue");
                        _terminal.getProperties().setPromptColor("white");
                        Venue tutorialVenue = _textIO.newEnumInputReader(Venue.class)
                                .read("Please add tutorial venue: ");
                        existingIndex.setTutorialVenue(tutorialVenue);
                        _terminal.getProperties().setPromptColor(Color.GREEN);
                        _terminal.println("Successfully added tutorial venue");
                        _terminal.getProperties().setPromptColor("white");
                    }
                    _textIO.newStringInputReader().withDefaultValue(" ")
                            .read("press enter to continue");
                }
                case 2 -> {
                    Hashtable<DayOfWeek, List<LocalTime>> originalLaboratoryTimings = existingIndex.getLaboratoryTimings();
                    Hashtable<DayOfWeek, List<LocalTime>> newLaboratoryTimings;
                    _terminal.println("Add/Update Laboratory Timing");
                    newLaboratoryTimings = Objects.requireNonNullElseGet(originalLaboratoryTimings, Hashtable::new);
                    DayOfWeek sessionDay = _textIO.newEnumInputReader(DayOfWeek.class)
                            .read("Enter Laboratory day: ");
                    List<LocalTime> sessionTiming = getValidTimeInput("laboratory");
                    newLaboratoryTimings.put(sessionDay, sessionTiming);

                    existingIndex.setTutorialTimings(newLaboratoryTimings);
                    if (course.isClashing(existingIndex)) {
                        existingIndex.setLaboratoryTimings(originalLaboratoryTimings);
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("Laboratory timing cannot clash with lecture timing");
                        _terminal.getProperties().setPromptColor("white");
                        _textIO.newStringInputReader().withDefaultValue(" ")
                                .read("press enter to continue");
                        break;
                    } else {
                        _terminal.getProperties().setPromptColor(Color.GREEN);
                        _terminal.println("Successfully added/updated Laboratory timing");
                    }
                    _terminal.getProperties().setPromptColor("white");

                    if (existingIndex.getLaboratoryVenue() == null) {
                        //Set lab venue if it does not exist
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("Laboratory timing cannot exist without venue");
                        _terminal.getProperties().setPromptColor("white");
                        Venue laboratoryVenue = _textIO.newEnumInputReader(Venue.class)
                                .read("Please add laboratory venue: ");
                        existingIndex.setLaboratoryVenue(laboratoryVenue);
                        _terminal.getProperties().setPromptColor(Color.GREEN);
                        _terminal.println("Successfully added laboratory venue");
                        _terminal.getProperties().setPromptColor("white");
                    }
                    _textIO.newStringInputReader().withDefaultValue(" ")
                            .read("press enter to continue");
                }
                //---------------Add/Update tutorial venue
                case 3 -> {
                    _terminal.println("Add/Update tutorial venue");
                    Venue tutorialVenue = _textIO.newEnumInputReader(Venue.class)
                            .read("add the venue for the tutorial session(s): ");
                    existingIndex.setTutorialVenue(tutorialVenue);
                    _terminal.getProperties().setPromptColor(Color.GREEN);
                    _terminal.println("Successfully updated tutorial venue");
                    _terminal.getProperties().setPromptColor("white");
                    if (existingIndex.getTutorialTimings() == null) {
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("Tutorial venue cannot exist without timing");
                        _terminal.getProperties().setPromptColor("white");
                        Hashtable<DayOfWeek, List<LocalTime>> originalTutorialTimings = existingIndex.getTutorialTimings();
                        Hashtable<DayOfWeek, List<LocalTime>> newTutorialTimings;
                        _terminal.println("Add/Update Tutorial Timing");
                        newTutorialTimings = Objects.requireNonNullElseGet(originalTutorialTimings, Hashtable::new);
                        DayOfWeek sessionDay = _textIO.newEnumInputReader(DayOfWeek.class)
                                .read("Enter tutorial day: ");
                        List<LocalTime> sessionTiming = getValidTimeInput("tutorial");
                        newTutorialTimings.put(sessionDay, sessionTiming);

                        existingIndex.setTutorialTimings(newTutorialTimings);
                        //Check if timing clashes with lecture
                        if (course.isClashing(existingIndex)) {
                            existingIndex.setTutorialTimings(originalTutorialTimings);
                            existingIndex.setTutorialVenue(null);
                            _terminal.getProperties().setPromptColor("red");
                            _terminal.println("Tutorial timing cannot clash with lecture timing");
                            _terminal.getProperties().setPromptColor("white");
                            _textIO.newStringInputReader().withDefaultValue(" ")
                                    .read("press enter to continue");
                            break;
                        } else {
                            _terminal.getProperties().setPromptColor(Color.GREEN);
                            _terminal.println("Successfully added tutorial timing");
                        }
                    }
                    _textIO.newStringInputReader().withDefaultValue(" ")
                            .read("press enter to continue");
                }
                //---------------Add/Update laboratory venue
                case 4 -> {
                    _terminal.println("Add/Update laboratory venue");
                    Venue laboratoryVenue = _textIO.newEnumInputReader(Venue.class)
                            .read("add the venue for the laboratory session(s): ");
                    existingIndex.setLaboratoryVenue(laboratoryVenue);
                    _terminal.getProperties().setPromptColor(Color.GREEN);
                    _terminal.println("Successfully updated laboratory venue");
                    _terminal.getProperties().setPromptColor("white");
                    //if a laboratory session has yet to be created, prompt user to input a laboratory session timing
                    if (existingIndex.getLaboratoryTimings() == null) {
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("Laboratory venue cannot exist without timing");
                        _terminal.getProperties().setPromptColor("white");
                        Hashtable<DayOfWeek, List<LocalTime>> originalLaboratoryTimings = existingIndex.getLaboratoryTimings();
                        Hashtable<DayOfWeek, List<LocalTime>> newLaboratoryTimings;
                        _terminal.println("Add/Update Laboratory Timing");
                        newLaboratoryTimings = Objects.requireNonNullElseGet(originalLaboratoryTimings, Hashtable::new);
                        DayOfWeek sessionDay = _textIO.newEnumInputReader(DayOfWeek.class)
                                .read("Enter Laboratory day: ");
                        List<LocalTime> sessionTiming = getValidTimeInput("laboratory");
                        newLaboratoryTimings.put(sessionDay, sessionTiming);

                        existingIndex.setTutorialTimings(newLaboratoryTimings);
                        //if the new laboratory timing clashes with a lecture timing
                        //revert the creation of the laboratory session
                        if (course.isClashing(existingIndex)) {
                            existingIndex.setLaboratoryTimings(originalLaboratoryTimings);
                            existingIndex.setLaboratoryVenue(null);
                            _terminal.getProperties().setPromptColor("red");
                            _terminal.println("Laboratory timing cannot clash with lecture timing");
                            _terminal.getProperties().setPromptColor("white");
                            _textIO.newStringInputReader().withDefaultValue(" ")
                                    .read("press enter to continue");
                            break;
                        } else {
                            _terminal.getProperties().setPromptColor(Color.GREEN);
                            _terminal.println("Successfully added laboratory timing");
                        }
                    }
                    _textIO.newStringInputReader().withDefaultValue(" ")
                            .read("press enter to continue");
                }
                //---------------Update class size
                case 5 -> {
                    int newMaxClassSize;
                    int currentMaxClassSize = existingIndex.getMaxClassSize();
                    //prompts user to re-input a new class size so long as the input is
                    //smaller than the original class size
                    do {
                        newMaxClassSize = _textIO.newIntInputReader()
                                .withMinVal(1)
                                .read("update maximum class size to: ");
                        if (newMaxClassSize < currentMaxClassSize) {
                            _terminal.getProperties().setPromptColor("red");
                            _terminal.println("New class size cannot be smaller than the original");
                            _terminal.getProperties().setPromptColor("white");
                        }
                    } while (newMaxClassSize < currentMaxClassSize);
                    existingIndex.setMaxClassSize(newMaxClassSize);
                    _terminal.getProperties().setPromptColor(Color.GREEN);
                    _terminal.println("Successfully updated maximum class size");
                    _terminal.getProperties().setPromptColor("white");
                    _textIO.newStringInputReader().withDefaultValue(" ")
                            .read("press enter to continue");
                }
                //---------------Delete index group
                case 6 -> {
                    try {
                        //get all enrolled students and students on wait list
                        IReadWriteRegistrationDataAccessObject registrationDataAccessObject =
                                Factory.getTextRegistrationDataAccessObject(this);
                        ArrayList<String> enrolledStudents = existingIndex.getEnrolledStudents();
                        Queue<String> waitingList = existingIndex.getWaitingList();
                        ArrayList<String> allStudents = new ArrayList<>();
                        allStudents.addAll(enrolledStudents);
                        allStudents.addAll(waitingList);

                        //get registration key for all students and
                        //delete them via the registrationDataAccessObject
                        for (String student : allStudents) {
                            RegistrationKey registrationKey = Factory.createRegistrationKey(student,
                                    course.getCourseCode(), index);
                            try {
                                registrationDataAccessObject.deleteRegistration(registrationKey);
                            } catch (IOException | ClassNotFoundException e) {
                                _terminal.println("file not found");
                            } catch (Exception e) {
                                _terminal.println("Error deleting registration");
                            }
                        }
                        course.deleteIndex(index);
                        _terminal.getProperties().setPromptColor(Color.green);
                        _terminal.println("Index deleted");
                    } catch (IOException | ClassNotFoundException e) {
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("Error reading file");
                    } catch (NonExistentIndexException ignore) {
                    } finally {
                        _terminal.getProperties().setPromptColor("white");
                        _textIO.newStringInputReader().withDefaultValue(" ").
                                read("press enter to continue");
                    }
                }
            }
        } while (option != 6 && option != 7);

        return course;
    }

    /**
     * Prints all data of students in enrolled and waiting list of a Index object of a course
     */
    private void printStudentListByIndexMenu() {
        try {
            IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
            String courseCode = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(courseDataAccessObject.getAllCourseCodes())
                    .read("print student list by index");
            Course course = courseDataAccessObject.getCourse(courseCode);

            String indexNumber = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(course.getListOfIndexNumbers()).read("index number: ");
            Index index = course.getIndex(Integer.parseInt(indexNumber));
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("Successfully retrieved student list");
            //Print enrolled students of the index
            _terminal.println(getStudentListByIndexString(index));
            _terminal.println("End of Student list");
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("file not found");
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ").read("press enter to continue");
        }
    }

     /**
     * Print all student enrolled in the course
     */
    private void printStudentListByCourseMenu() {
        try {
            String courseCode;
            IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
            courseCode = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(courseDataAccessObject.getAllCourseCodes())
                    .read("print student list by course");
            Course course = courseDataAccessObject.getCourse(courseCode);
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("Successfully retrieved student list");
            _terminal.println("courseCode: " + course.getCourseCode() + ",\t" + "courseName: " + course.getCourseName());
            //Print out students enrolled in the course
            for (String indexNumber : course.getListOfIndexNumbers()) {
                Index index = course.getIndex(Integer.parseInt(indexNumber));
                _terminal.println(getStudentListByIndexString(index));
                _terminal.println();
            }
            _terminal.getProperties().setPromptColor("white");
            _terminal.println("End of Student list");
            _terminal.getProperties().setPromptColor("white");
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("file not found");
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ")
                    .read("press enter to continue");
        }
    }

    /**
     * Prints the student menu for admin
     * Allows admin to access those functions in student menu
     */
    private void loginAsStudentMenu() {
        try {
            _terminal.setBookmark("log in as student");
            _terminal.println("Update user courses/ Sign in as student");
            String username = _textIO.newStringInputReader()
                    .read("Enter Username:");
            String password = _textIO.newStringInputReader()
                    .withMinLength(6)
                    .withInputMasking(true)
                    .read("Enter Password: ");
            //Authenticate the username and password
            AbstractUser abstractUser = Factory.getTextUserDataAccessObject(this).authenticate(username, password);
            if (abstractUser == null || abstractUser.getUserType() == UserType.ADMIN) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("Invalid User");
            } else {
                IReadWriteRegistrationDataAccessObject registrationDataAccessObject =
                        Factory.getTextRegistrationDataAccessObject(this);
                RegistrationPeriod temporaryRegistrationPeriod = Factory.createRegistrationPeriod(LocalDateTime.now().minusDays(2),
                        LocalDateTime.now().plusDays(2));
                RegistrationPeriod currentRegistrationPeriod = registrationDataAccessObject.getRegistrationPeriod();
                registrationDataAccessObject.updateRegistrationPeriod(temporaryRegistrationPeriod);
                ISession session = Factory.createSession(abstractUser);
                try {
                    session.run();
                } catch (SecurityException ignored) {
                }
                registrationDataAccessObject.updateRegistrationPeriod(currentRegistrationPeriod);
                _terminal.getProperties().setPromptColor(Color.GREEN);
                _terminal.println("Successfully logged out");
            }
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("Error reading file");
        } catch (PasswordStorage.CannotPerformOperationException | PasswordStorage.InvalidHashException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("Error unhashing password");
        } catch (IdenticalRegistrationPeriodException ignored) {
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ")
                    .read("press enter to continue");
            _terminal.resetToBookmark("clear");
            _terminal.println(admin);
        }
    }

    /**
     * Overrides an existing course
     */
    private void updateCourse(Course newCourse) {
        try {
            IReadWriteCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
            courseDataAccessObject.updateCourse(newCourse);
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("Successfully updated course");
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("file not found");
        } finally {
            _terminal.getProperties().setPromptColor("white");
        }
    }

    /**
     * Appends data of students in enrolled and waiting list of a Index object into a String
     * @param index Index object
     * @return A String as described above
     */
    private String getStudentListByIndexString(Index index) {
        StringBuilder str = new StringBuilder();
        try {
            IReadWriteUserDataAccessObject userDataAccessObject = Factory.getTextUserDataAccessObject(this);
            ArrayList<String> enrolledStudents = index.getEnrolledStudents();
            Queue<String> waitingListStudents = index.getWaitingList();
            str.append("indexNumber: ").append(index.getIndexNumber()).append('\n');
            if (enrolledStudents.isEmpty()) {
                str.append("no enrolled students");
            } else {
                str.append("enrolled students: ").append('\n');
                for (String matricNumber : enrolledStudents) {
                    Student student = userDataAccessObject.getStudent(matricNumber);
                    str.append("name: \t\t").append(student.getName()).append('\n');
                    str.append("gender: \t").append(student.getGender()).append('\n');
                    str.append("nationality: \t").append(student.getNationality()).append('\n');
                }
                if (waitingListStudents.isEmpty()) {
                    str.append("no students in waiting list");
                } else {
                    str.append("waiting list students: ").append('\n');
                    for (String matricNumber : waitingListStudents) {
                        Student student = userDataAccessObject.getStudent(matricNumber);
                        str.append("name: \t\t").append(student.getName()).append('\n');
                        str.append("gender: \t").append(student.getGender()).append('\n');
                        str.append("nationality: \t").append(student.getNationality()).append('\n');
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("file not found");
            str.append(" ");
        }
        return str.toString();
    }

    /**
     * Get valid time input
     * @return List of LocalTime that represents the start and end time
     */
    private List<LocalTime> getValidTimeInput(String sessionType) {
        List<LocalTime> startEndTime = new ArrayList<>();
        boolean proceed;
        String startTime;
        String schoolStartTime = "07:30";
        String schoolEndTime = "21:30";
        int maxDuration = 4;
        int duration = _textIO.newIntInputReader()
                .withMinVal(1).withMaxVal(maxDuration)
                .read("enter the duration (1-" + maxDuration + ") of the " + sessionType + "(hrs): ");
        _terminal.setBookmark("start time");
        do {
            //Prompt user to enter start time of the session and reads it
            startTime = _textIO.newStringInputReader().read("enter the start time in HH:MM (30 min interval, e.g. 16:30): ");
            //Checks if input is valid
            proceed = InputValidator.validateTimeInput(startTime) &&
                    InputValidator.validateTimeInput(startTime, schoolStartTime, schoolEndTime, duration);
            if (!proceed) {
                _terminal.resetToBookmark("start time");
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("timing is invalid. school should start earliest at 07:30 and end latest by 21:30." +
                        "\nAND classes should start at a 30min interval");
                _terminal.getProperties().setPromptColor("white");
            }
        } while (!proceed);

        LocalTime classStartTime = LocalTime.parse(startTime);
        LocalTime classEndTime = classStartTime.plusHours(duration);
        startEndTime.add(classStartTime);
        startEndTime.add(classEndTime);

        return startEndTime;
    }
}