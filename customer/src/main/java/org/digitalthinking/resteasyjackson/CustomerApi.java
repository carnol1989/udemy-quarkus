package org.digitalthinking.resteasyjackson;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.digitalthinking.entites.Customer;
import org.digitalthinking.entites.Product;
import org.digitalthinking.repositories.CustomerRepository;
import org.jboss.resteasy.reactive.RestPath;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Slf4j
@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerApi {

    @Inject
    CustomerRepository cr;

    @GET
    public Uni<List<PanacheEntityBase>> list() {
        return Customer.listAll(Sort.by("names"));
    }

    @GET
    @Path("/{id}")
    public Uni<PanacheEntityBase> findCustomerById(@PathParam("id") Long id) {
        return Customer.findById(id);
    }

    @GET
    @Path("using-repository")
    public Uni<List<Customer>> listUsingRepository() {
        return cr.findAll().list();
    }

    @GET
    @Path("/{id}/product")
    public Uni<Customer> findCustomerByIdWithProduct(@PathParam("id") Long id) {
        return cr.findCustomerByIdWithProduct(id);
    }

    @POST
    public Uni<Response> add(Customer c) {
        return cr.add(c);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@RestPath Long id, Customer c) {
        return cr.update(id, c);
    }

    @DELETE
    @Path("/{Id}")
    public Uni<Response> delete(@PathParam("Id") Long Id) {
        return cr.delete(Id);
    }

    @GET
    @Path("/products-graphql")
    @Blocking
    public  List<Product> getProductsGraphQl() throws Exception {
        return cr.getProductsGraphQl();
    }

    @GET
    @Path("/{id}/product-graphql")
    @Blocking
    public  Product getProductsGraphQl(@PathParam("id") Long id) throws Exception {
        return cr.getByIdProductGraphQl(id);
    }

}
