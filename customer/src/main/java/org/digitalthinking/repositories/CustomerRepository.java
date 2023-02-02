package org.digitalthinking.repositories;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import org.digitalthinking.entites.Customer;
import org.digitalthinking.entites.CustomerView;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CustomerRepository {
    @Inject
    EntityManager em;

    @Inject
    CriteriaBuilderFactory cbf;

    @Inject
    EntityViewManager evm;

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
    public List<CustomerView> listCustomers(){
        //List<Customer> customers = em.createQuery("select c from Customer c").getResultList();

        CriteriaBuilder<Customer> cb = cbf.create(em, Customer.class);
        List<CustomerView> customers = evm.applySetting(EntityViewSetting.create(CustomerView.class), cb).getResultList();

        return customers;
    }
}
