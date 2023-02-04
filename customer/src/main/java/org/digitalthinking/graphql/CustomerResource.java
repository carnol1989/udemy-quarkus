package org.digitalthinking.graphql;

import io.smallrye.mutiny.Uni;
import org.digitalthinking.entites.Customer;
import org.digitalthinking.repositories.CustomerRepository;
import org.eclipse.microprofile.graphql.*;

import javax.inject.Inject;
import java.util.List;

@GraphQLApi
public class CustomerResource {

    @Inject
    CustomerRepository repository;

    @Query("allCustomers")
    @Description("Get all customers from a database")
    public Uni<List<Customer>> getAllCustomer() {
        return repository.listAll();
    }

    @Query
    @Description("Get a customer from a database")
    public Uni<Customer> getCustomer(@Name("customerId") Long id) {
        return repository.findById(id);
    }

    @Mutation
    public  Uni<Customer> addCustomer(Customer customer) {
        return repository.addMutation(customer);
    }

    @Mutation
    public Uni<Boolean> deleteCustomer(Long id) {
        return repository.deleteMutation(id);
    }

}
