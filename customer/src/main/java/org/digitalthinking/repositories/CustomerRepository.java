package org.digitalthinking.repositories;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import org.digitalthinking.entites.Customer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, Long> {

}
