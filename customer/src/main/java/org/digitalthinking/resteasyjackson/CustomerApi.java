package org.digitalthinking.resteasyjackson;

import org.digitalthinking.entites.Customer;
import org.digitalthinking.repositories.CustomerRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerApi {
 @Inject
 CustomerRepository pr;

    @GET
    public List<Customer> list() {
        return pr.listCustomers();
    }

    @GET
    @Path("/{id}")
    public Customer findCustomerById(@PathParam("id") Long id) {
        return pr.getCustomerById(id);
    }

    @POST
    public Response add(Customer p) {
        pr.createdCustomer(p);
        return Response.ok().build();
    }

    @PUT
    public Response update(Customer p) {
        Customer customer = pr.getCustomerById(p.getId());
        customer.setCode(p.getCode());
        customer.setAccountNumber(p.getAccountNumber());
        customer.setSurname(p.getSurname());
        customer.setPhone(p.getPhone());
        customer.setAddress(p.getAddress());
        customer.setProducts(p.getProducts());
        pr.updateCustomer(customer);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{Id}")
    public Response delete(@QueryParam("Id") Long Id) {
        Customer customer = pr.getCustomerById(Id);
        pr.deleteCustomer(customer);
        return Response.ok().build();
    }

}
