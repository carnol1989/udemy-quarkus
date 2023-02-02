package org.digitalthinking.resteasyjackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.digitalthinking.entites.Customer;
import org.digitalthinking.entites.Product;
import org.digitalthinking.repositories.CustomerRepository;
import org.jboss.resteasy.reactive.RestPath;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@Slf4j
@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerApi {

    @Inject
    CustomerRepository cr;

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
        return Uni.combine().all().unis(getCustomerRective(id), getAllProductReactive())
                .combinedWith((v1, v2) -> {
                    v1.getProducts().forEach(product -> {
                        v2.forEach(p -> {
                            log.info("Ids are: " + product.getProduct() +" = " +p.getId());
                            if (product.getProduct() == p.getId()) {
                                product.setName(p.getName());
                                product.setDescription(p.getDescription());
                            }
                        });
                    });
                    return v1;
                });
    }

    @POST
    public Uni<Response> add(Customer c) {
        return Panache.withTransaction(c::persist)
                .replaceWith(Response.ok(c).status(CREATED)::build);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@RestPath Long id, Customer c) {
        if (c == null || c.getCode() == null) {
            throw new WebApplicationException("Product code was not set on request.", HttpResponseStatus.UNPROCESSABLE_ENTITY.code());
        }
        return Panache
                .withTransaction(() -> Customer.<Customer> findById(id)
                        .onItem().ifNotNull().invoke(entity -> {
                            entity.setNames(c.getNames());
                            entity.setAccountNumber(c.getAccountNumber());
                            entity.setCode(c.getCode());
                        })
                )
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("/{Id}")
    public Uni<Response> delete(@PathParam("Id") Long Id) {
        return Panache.withTransaction(() -> Customer.deleteById(Id))
                .map(deleted -> deleted
                        ? Response.ok().status(NO_CONTENT).build()
                        : Response.ok().status(NOT_FOUND).build());
    }

    private Uni<Customer> getCustomerRective(Long idCostumer) {
        return Customer.findById(idCostumer);
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
