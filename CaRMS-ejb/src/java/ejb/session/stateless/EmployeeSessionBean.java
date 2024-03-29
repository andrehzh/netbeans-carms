/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Employee;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.DeleteEmployeeException;
import util.exception.EmployeeEmailExistsException;
import util.exception.EmployeeNotFoundException;
import util.exception.InputDataValidationException;
import util.exception.InvalidLoginCredentialException;
import util.exception.UnknownPersistenceException;
import util.exception.UpdateEmployeeException;

/**
 *
 * @author tian
 */
@Stateless
public class EmployeeSessionBean implements EmployeeSessionBeanRemote, EmployeeSessionBeanLocal {

    @PersistenceContext(unitName = "CaRMS-ejbPU")
    private EntityManager em;

    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    public EmployeeSessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public Long createNewEmployee(Employee employee) throws UnknownPersistenceException, InputDataValidationException, EmployeeEmailExistsException {
        Set<ConstraintViolation<Employee>> constraintViolations = validator.validate(employee);

        if (constraintViolations.isEmpty()) {
            try {
                em.persist(employee);
                em.flush();
                return employee.getEmployeeId();
            } catch (PersistenceException ex) {
                if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                    if (ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException")) {
                        throw new EmployeeEmailExistsException();
                    } else {
                        throw new UnknownPersistenceException(ex.getMessage());
                    }
                } else {
                    throw new UnknownPersistenceException(ex.getMessage());
                }
            }
        } else {
            throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
        }
    }

    @Override
    public Employee retrieveEmployeeById(Long id) throws EmployeeNotFoundException {
        Employee employee = em.find(Employee.class, id);
        if (employee != null) {
            return employee;
        } else {
            throw new EmployeeNotFoundException("Employee " + id.toString() + " does not exist!");
        }
    }

    @Override
    public Employee retrieveEmployeeByEmployeeEmail(String email) throws EmployeeNotFoundException {
        try {
            Query query = em.createQuery("SELECT s FROM Employee s WHERE s.employeeEmail = :inEmail");
            query.setParameter("inEmail", email);
            return (Employee) query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException ex) {
            throw new EmployeeNotFoundException("Employee Email " + email + " does not exist!");
        }
    }

    @Override
    public List<Employee> retrieveAllEmployees() {
        Query query = em.createQuery("SELECT e FROM Employee e");

        return query.getResultList();
    }

    @Override
    public void updateEmployee(Employee employee) throws EmployeeNotFoundException, InputDataValidationException, UpdateEmployeeException {
        if (employee != null && employee.getEmployeeId() != null) {
            Set<ConstraintViolation<Employee>> constraintViolations = validator.validate(employee);

            if (constraintViolations.isEmpty()) {
                Employee employeeToUpdate = retrieveEmployeeById(employee.getEmployeeId());
                if (employeeToUpdate.getEmployeeEmail().equals(employee.getEmployeeEmail())) {
                    employeeToUpdate.setEmployeeName(employee.getEmployeeName());
                    employeeToUpdate.setEmployeePassword(employee.getEmployeePassword());
                    employeeToUpdate.setAccessRight(employee.getAccessRight());
                } else {
                    throw new UpdateEmployeeException("UpdateEmployeeException");
                }
            } else {
                throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
            }
        } else {
            throw new EmployeeNotFoundException("Employee " + employee.getEmployeeId().toString() + " does not exist!");
        }
    }

    @Override
    public void deleteEmployee(Long employeeId) throws EmployeeNotFoundException, DeleteEmployeeException {
        Employee employeeToRemove = retrieveEmployeeById(employeeId);
        if (!employeeToRemove.getTransitDriverDispatchRecords().isEmpty()) {
            throw new DeleteEmployeeException("Employee " + employeeId.toString() + " is associated with existing Transit Driver Dispatch Record(s) and cannot be deleted!");
        } else {
            em.remove(employeeToRemove);
        }
    }

    @Override
    public Employee employeeLogin(String email, String password) throws InvalidLoginCredentialException {
        try {
            Employee employee = retrieveEmployeeByEmployeeEmail(email);

            if (employee.getEmployeePassword().equals(password)) {
                return employee;
            } else {
                throw new InvalidLoginCredentialException("Email does not exist or invalid password!");
            }
        } catch (EmployeeNotFoundException ex) {
            throw new InvalidLoginCredentialException("Email does not exist or invalid password!");
        }
    }

    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<Employee>> constraintViolations) {
        String msg = "Input data validation error!:";

        for (ConstraintViolation constraintViolation : constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }

        return msg;
    }
}
