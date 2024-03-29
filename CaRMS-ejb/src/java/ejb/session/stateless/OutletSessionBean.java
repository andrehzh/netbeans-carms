/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Outlet;
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
import util.exception.InputDataValidationException;
import util.exception.OutletNameExistException;
import util.exception.OutletNotFoundException;
import util.exception.UnknownPersistenceException;
import util.exception.UpdateOutletException;

/**
 *
 * @author tian
 */
@Stateless
public class OutletSessionBean implements OutletSessionBeanRemote, OutletSessionBeanLocal {

    @PersistenceContext(unitName = "CaRMS-ejbPU")
    private EntityManager em;

    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    public OutletSessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public Long createNewOutlet(Outlet outlet) throws OutletNameExistException, UnknownPersistenceException, InputDataValidationException {
        Set<ConstraintViolation<Outlet>> constraintViolations = validator.validate(outlet);

        if (constraintViolations.isEmpty()) {
            try {
                em.persist(outlet);
                em.flush();
                return outlet.getOutletId();
            } catch (PersistenceException ex) {
                if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                    if (ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException")) {
                        throw new OutletNameExistException();
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
    public Outlet retrieveOutletById(Long id) throws OutletNotFoundException {
        Outlet outlet = em.find(Outlet.class, id);
        if (outlet != null) {
            return outlet;
        } else {
            throw new OutletNotFoundException("Outlet " + id.toString() + " does not exist!");
        }
    }

    @Override
    public List<Outlet> retrieveAllOutlets() {
        Query query = em.createQuery("SELECT o FROM Outlet o");

        return query.getResultList();
    }
    
    /**
     *
     * @param outletName
     * @return
     * @throws OutletNotFoundException
     */
    @Override
    public Outlet retrieveOutletByOutletName(String outletName) throws OutletNotFoundException {
        try {
        Query query = em.createQuery("SELECT o FROM Outlet o WHERE o.outletName = :inOutletName");
        query.setParameter("inOutletName", outletName);
        
        return (Outlet) query.getSingleResult(); 
        } catch (PersistenceException ex) {
            throw new OutletNotFoundException();
        }
        
    }

    @Override
    public void updateOutlet(Outlet outlet) throws OutletNotFoundException, InputDataValidationException, UpdateOutletException {
        if (outlet != null && outlet.getOutletId() != null) {
            Set<ConstraintViolation<Outlet>> constraintViolations = validator.validate(outlet);

            if (constraintViolations.isEmpty()) {
                Outlet outletToUpdate = retrieveOutletById(outlet.getOutletId());
                try {
                    outletToUpdate.setOutletName(outlet.getOutletName());
                    outletToUpdate.setOpeningTime(outlet.getOpeningTime());
                    outletToUpdate.setClosingTime(outlet.getClosingTime());
                } catch (PersistenceException ex) {
                    throw new UpdateOutletException("UpdateOutletException");
                }
            } else {
                throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
            }
        } else {
            throw new OutletNotFoundException("Outlet " + outlet.getOutletId().toString() + " does not exist!");
        }
    }

    @Override
    public void deleteOutlet(Long outletId) throws OutletNotFoundException //, DeleteOutletException
    {
        Outlet outletToRemove = retrieveOutletById(outletId);
        if (outletToRemove != null) {
            em.remove(outletToRemove);
        } else {
            throw new OutletNotFoundException("Outlet " + outletId.toString() + " does not exist!");
        }
    }

    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<Outlet>> constraintViolations) {
        String msg = "Input data validation error!:";

        for (ConstraintViolation constraintViolation : constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }

        return msg;
    }
}
