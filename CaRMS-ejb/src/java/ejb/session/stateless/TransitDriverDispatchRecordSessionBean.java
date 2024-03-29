/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.TransitDriverDispatchRecord;
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
import util.exception.DeleteTransitDriverDispatchRecordException;
import util.exception.InputDataValidationException;
import util.exception.TransitDriverDispatchRecordNotFoundException;
import util.exception.UnknownPersistenceException;
import util.exception.UpdateTransitDriverDispatchRecordException;

/**
 *
 * @author tian
 */
@Stateless
public class TransitDriverDispatchRecordSessionBean implements TransitDriverDispatchRecordSessionBeanRemote, TransitDriverDispatchRecordSessionBeanLocal {

    @PersistenceContext(unitName = "CaRMS-ejbPU")
    private EntityManager em;

    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    public TransitDriverDispatchRecordSessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public Long createNewTransitDriverDispatchRecord(TransitDriverDispatchRecord transitDriverDispatchRecord) throws UnknownPersistenceException, InputDataValidationException {
        Set<ConstraintViolation<TransitDriverDispatchRecord>> constraintViolations = validator.validate(transitDriverDispatchRecord);

        if (constraintViolations.isEmpty()) {
            try {
                em.persist(transitDriverDispatchRecord);
                em.flush();
                return transitDriverDispatchRecord.getTransitDriverDispatchRecordId();
            } catch (PersistenceException ex) {
                if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                    throw new UnknownPersistenceException(ex.getMessage());

                } else {
                    throw new UnknownPersistenceException(ex.getMessage());
                }
            }
        } else {
            throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
        }
    }

    @Override
    public TransitDriverDispatchRecord retrieveTransitDriverDispatchRecordById(Long id) throws TransitDriverDispatchRecordNotFoundException {
        TransitDriverDispatchRecord transitDriverDispatchRecord = em.find(TransitDriverDispatchRecord.class, id);
        if (transitDriverDispatchRecord != null) {
            return transitDriverDispatchRecord;
        } else {
            throw new TransitDriverDispatchRecordNotFoundException("Transit Driver Dispatch Record " + id.toString() + " does not exist!");
        }
    }

    @Override
    public List<TransitDriverDispatchRecord> retrieveAllTransitDriverDispatchRecords() {
        Query query = em.createQuery("SELECT dispatchRecord FROM TransitDriverDispatchRecord dispatchRecord");

        return query.getResultList();
    }

    @Override
    public void updateTransitDriverDispatchRecord(TransitDriverDispatchRecord transitDriverDispatchRecord) throws TransitDriverDispatchRecordNotFoundException, InputDataValidationException, UpdateTransitDriverDispatchRecordException {
        if (transitDriverDispatchRecord != null && transitDriverDispatchRecord.getTransitDriverDispatchRecordId() != null) {
            Set<ConstraintViolation<TransitDriverDispatchRecord>> constraintViolations = validator.validate(transitDriverDispatchRecord);

            if (constraintViolations.isEmpty()) {
                TransitDriverDispatchRecord transitDriverDispatchRecordToUpdate = retrieveTransitDriverDispatchRecordById(transitDriverDispatchRecord.getTransitDriverDispatchRecordId());
                try {
                    transitDriverDispatchRecordToUpdate.setDispatchDate(transitDriverDispatchRecord.getDispatchDate());
                    transitDriverDispatchRecordToUpdate.setIsCompleted(transitDriverDispatchRecord.isIsCompleted());
                } catch (PersistenceException ex) {
                    throw new UpdateTransitDriverDispatchRecordException("UpdateTransitDriverDispatchRecordException");
                }

            } else {
                throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
            }
        } else {
            throw new TransitDriverDispatchRecordNotFoundException("Transit Driver Dispatch Record " + transitDriverDispatchRecord.getTransitDriverDispatchRecordId().toString() + " does not exist!");
        }
    }

    @Override
    public void deleteTransitDriverDispatchRecord(Long transitDriverDispatchRecordId) throws TransitDriverDispatchRecordNotFoundException, DeleteTransitDriverDispatchRecordException {
        TransitDriverDispatchRecord transitDriverDispatchRecordToRemove = retrieveTransitDriverDispatchRecordById(transitDriverDispatchRecordId);
        try {
            em.remove(transitDriverDispatchRecordToRemove);
            return;
        } catch (PersistenceException ex) {
            throw new DeleteTransitDriverDispatchRecordException("DeleteTransitDriverDispatchRecordException");
        }
    }

    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<TransitDriverDispatchRecord>> constraintViolations) {
        String msg = "Input data validation error!:";

        for (ConstraintViolation constraintViolation : constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }

        return msg;
    }
}
