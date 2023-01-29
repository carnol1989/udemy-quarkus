package org.digitalthinking.repositories;

import org.digitalthinking.entites.Customer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CustomerRepository {
    @Inject
    EntityManager em;

    @Transactional
    public void createdCustomer(Customer p){
        em.persist(p);
    }

    @Transactional
    public void updateCustomer(Customer p) {
        em.merge(p);
    }

    @Transactional
    public void deleteCustomer(Customer p){
        em.remove(p);
    }

    @Transactional
    public Customer getCustomerById(Long customerId) {
        Customer customer = em.find(Customer.class, customerId);
        return customer;
    }

    @Transactional
    public List<Customer> listCustomers(){
        List<Customer> customers = em.createQuery("select c from Customer c").getResultList();
        return customers;
    }
}
