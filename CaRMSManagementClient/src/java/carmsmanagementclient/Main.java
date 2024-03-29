/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carmsmanagementclient;

import ejb.session.stateless.CarModelSessionBeanRemote;
import ejb.session.stateless.CarSessionBeanRemote;
import ejb.session.stateless.CategorySessionBeanRemote;

import ejb.session.stateless.EjbTimerSessionBeanRemote;

import ejb.session.stateless.CustomerSessionBeanRemote;

import ejb.session.stateless.EmployeeSessionBeanRemote;
import ejb.session.stateless.OutletSessionBeanRemote;
import ejb.session.stateless.RentalRateSessionBeanRemote;
import ejb.session.stateless.ReservationSessionBeanRemote;
import ejb.session.stateless.TransitDriverDispatchRecordSessionBeanRemote;
import javax.ejb.EJB;

/**
 *
 * @author andre
 */
public class Main {

    @EJB
    private static EjbTimerSessionBeanRemote ejbTimerSessionBeanRemote;
    @EJB
    private static EmployeeSessionBeanRemote employeeSessionBeanRemote;
    @EJB
    private static OutletSessionBeanRemote outletSessionBeanRemote;
    @EJB
    private static RentalRateSessionBeanRemote rentalRateSessionBeanRemote;
    @EJB
    private static CategorySessionBeanRemote categorySessionBeanRemote;
    @EJB
    private static CarModelSessionBeanRemote carModelSessionBeanRemote;
    @EJB
    private static CarSessionBeanRemote carSessionBeanRemote;
    @EJB
    private static TransitDriverDispatchRecordSessionBeanRemote transitDriverDispatchRecordSessionBeanRemote;
    @EJB
    private static CustomerSessionBeanRemote customerSessionBeanRemote;
    @EJB
    private static ReservationSessionBeanRemote reservationSessionBeanRemote;

    public static void main(String[] args) {
        MainApp mainApp = new MainApp(employeeSessionBeanRemote, outletSessionBeanRemote, rentalRateSessionBeanRemote, categorySessionBeanRemote, carModelSessionBeanRemote, carSessionBeanRemote, transitDriverDispatchRecordSessionBeanRemote, ejbTimerSessionBeanRemote, customerSessionBeanRemote, reservationSessionBeanRemote);

        mainApp.runApp();
    }

}
