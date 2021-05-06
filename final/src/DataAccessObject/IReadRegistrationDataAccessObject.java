package DataAccessObject;

import ValueObject.RegistrationPeriod;

/**
 * Interface of read only RegistrationDataAccessObject
 */
public interface IReadRegistrationDataAccessObject {
    /**
     * get registration period
     * @return RegistrationPeriod object that contains the current start and end time
     */
    RegistrationPeriod getRegistrationPeriod();
}
