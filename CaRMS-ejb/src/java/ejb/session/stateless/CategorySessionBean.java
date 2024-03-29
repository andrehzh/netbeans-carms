/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Category;
import entity.Employee;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CategoryNameExistsException;
import util.exception.CategoryNotFoundException;
import util.exception.DeleteCategoryException;
import util.exception.EmployeeNotFoundException;
import util.exception.InputDataValidationException;
import util.exception.UnknownPersistenceException;
import util.exception.UpdateCategoryException;

/**
 *
 * @author tian
 */
@Stateless
public class CategorySessionBean implements CategorySessionBeanRemote, CategorySessionBeanLocal {

    @PersistenceContext(unitName = "CaRMS-ejbPU")
    private EntityManager em;

    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    public CategorySessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public Long createNewCategory(Category category) throws UnknownPersistenceException, InputDataValidationException, CategoryNameExistsException {
        Set<ConstraintViolation<Category>> constraintViolations = validator.validate(category);

        if (constraintViolations.isEmpty()) {
            try {
                em.persist(category);
                em.flush();
                return category.getCategoryId();
            } catch (PersistenceException ex) {
                if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                    if (ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException")) {
                        throw new CategoryNameExistsException();
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
    public Category retrieveCategoryById(Long id) throws CategoryNotFoundException {
        Category category = em.find(Category.class, id);
        if (category != null) {
            return category;
        } else {
            throw new CategoryNotFoundException("Category " + id.toString() + " does not exist!");
        }
    }

    @Override
    public List<Category> retrieveAllCategories() {
        Query query = em.createQuery("SELECT cat FROM Category cat");

        return query.getResultList();
    }

    @Override
    public Category retrieveCategoryByCategoryName(String categoryName) throws CategoryNotFoundException {
        try {
            Query query = em.createQuery("SELECT c FROM Category c WHERE c.categoryName = :inCategoryName");
            query.setParameter("inCategoryName", categoryName);

            return (Category) query.getSingleResult();
        } catch (PersistenceException ex) {
            throw new CategoryNotFoundException();
        }

    }

    @Override
    public void updateCategory(Category category) throws CategoryNotFoundException, InputDataValidationException, UpdateCategoryException {
        if (category != null && category.getCategoryId() != null) {
            Set<ConstraintViolation<Category>> constraintViolations = validator.validate(category);

            if (constraintViolations.isEmpty()) {
                Category categoryToUpdate = retrieveCategoryById(category.getCategoryId());
                if (categoryToUpdate.getCategoryName().equals(category.getCategoryName())) {
                    categoryToUpdate.setCategoryDesc(category.getCategoryDesc());
                } else {
                    throw new UpdateCategoryException("UpdateCategoryException");
                }
            } else {
                throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
            }
        } else {
            throw new CategoryNotFoundException("Category " + category.getCategoryId().toString() + " does not exist!");
        }
    }

    @Override
    public void deleteCategory(Long categoryId) throws CategoryNotFoundException, DeleteCategoryException {
        Category categoryToRemove = retrieveCategoryById(categoryId);
        if (!categoryToRemove.getRentalRates().isEmpty()) {
            throw new DeleteCategoryException("Category " + categoryId.toString() + " is associated with existing rental rate(s) and cannot be deleted!");
        } else if (!categoryToRemove.getCarModels().isEmpty()) {
            throw new DeleteCategoryException("Category " + categoryId.toString() + " is associated with existing car model(s) and cannot be deleted!");
        } else {
            em.remove(categoryToRemove);
        }
    }

    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<Category>> constraintViolations) {
        String msg = "Input data validation error!:";

        for (ConstraintViolation constraintViolation : constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }

        return msg;
    }
}
