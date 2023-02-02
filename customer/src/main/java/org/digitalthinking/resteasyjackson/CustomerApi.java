package org.digitalthinking.resteasyjackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.digitalthinking.entites.Customer;
import org.digitalthinking.entites.CustomerView;
import org.digitalthinking.entites.Product;
import org.digitalthinking.repositories.CustomerRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerApi {
    @Inject
    CustomerRepository pr;

    @Inject
    Vertx vertx;

    private WebClient webClient;//permite comunicarnos con otro microservicio

    @PostConstruct
    void initialize() {
        this.webClient = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost("localhost")
                        .setDefaultPort(8081).setSsl(false).setTrustAll(true));
    }

    @GET
    @Blocking
    public List<CustomerView> list() {
        return pr.listCustomers();
    }

    @GET
    @Path("/{id}")
    @Blocking
    public Customer findCustomerById(@PathParam("id") Long id) {
        return pr.getCustomerById(id);
    }

    @GET
    @Path("/{id}/product")
    @Blocking
    public Uni<Customer> findCustomerByIdWithProduct(@PathParam("id") Long id) {
        return Uni.combine().all().unis(getCustomerRective(id), getAllProductReactive())
                .combinedWith((v1, v2) -> {
                    v1.getProducts().forEach(product -> {
                        v2.forEach(p -> {
                            if (product.getId().equals(p.getId())) {
                                product.setName(p.getName());
                                product.setDescription(p.getDescription());
                            }
                        });
                    });
                    return v1;
                });
    }

    @POST
    @Blocking
    public Response add(Customer p) {
        pr.createdCustomer(p);
        return Response.ok().build();
    }

    @PUT
    @Blocking
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
    @Blocking
    public Response delete(@PathParam("Id") Long Id) {
        Customer customer = pr.getCustomerById(Id);
        pr.deleteCustomer(customer);
        return Response.ok().build();
    }

    private Uni<Customer> getCustomerRective(Long idConsumer) {
        Customer customer = pr.getCustomerById(idConsumer);
        Uni<Customer> item = Uni.createFrom().item(customer);
        return item;
    }

    private Uni<List<Product>> getAllProductReactive() {
        return webClient.get(8081, "localhost", "/product").send()
                .onFailure().invoke(res -> log.error("Error recuperando productos", res))
                .onItem().transform(res -> {
                    List<Product> lista = new ArrayList<>();
                    JsonArray objects = res.bodyAsJsonArray();
                    objects.forEach(p -> {
                        log.info("See Objects: " + objects);
                        ObjectMapper objectMapper = new ObjectMapper();
                        Product product = null;
                        try {
                            product = objectMapper.readValue(p.toString(), Product.class);
                        } catch (JsonProcessingException e) {
                            log.error("Error JSON string to pojo: " + e.getMessage() + "-" + e.getCause());
                        }
                        lista.add(product);
                    });
                    return lista;
                });
    }

}
