package org.digitalthinking.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.digitalthinking.entites.Customer;
import org.digitalthinking.entites.Product;
import org.jboss.resteasy.reactive.RestPath;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static javax.ws.rs.core.Response.Status.*;

@Slf4j
@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, Long> {

    @Inject
    Vertx vertx;

    private WebClient webClient;//permite comunicarnos con otro microservicio

    @Inject
            @GraphQLClient("product-dynamic-client")
    DynamicGraphQLClient dynamicGraphQLClient;

    @PostConstruct
    void initialize() {
        this.webClient = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost("localhost")
                        .setDefaultPort(8081).setSsl(false).setTrustAll(true));
    }

    public Uni<Response> add(Customer c) {
        c.getProducts().forEach(p -> p.setCustomer(c));
        return Panache.withTransaction(c::persist)
                .replaceWith(Response.ok(c).status(CREATED)::build);
    }
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

    public Uni<Response> delete(@PathParam("Id") Long Id) {
        return Panache.withTransaction(() -> Customer.deleteById(Id))
                .map(deleted -> deleted
                        ? Response.ok().status(NO_CONTENT).build()
                        : Response.ok().status(NOT_FOUND).build());
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

    public Uni<Boolean> deleteMutation(@PathParam("Id") Long Id) {
        return Panache.withTransaction(() -> Customer.deleteById(Id));
    }

    public Uni<Customer> addMutation(Customer c) {
        c.getProducts().forEach(p -> p.setCustomer(c));
        return Panache.withTransaction(c::persist)
                .replaceWith(c);
    }

    public List<Product> getProductsGraphQl() throws Exception {
        Document query = document(
                operation(
                        field("allProducts",
                                field("id"),
                                field("code"),
                                field("name"),
                                field("description")
                        )
                )
        );

        io.smallrye.graphql.client.Response response = dynamicGraphQLClient.executeSync(query);
        return response.getList(Product.class, "allProducts");
    }

    public Product getByIdProductGraphQl(Long id) throws Exception {
        Document query = document(
                operation(
                        field("productById(productId:"+id+")",
                                field("id"),
                                field("code"),
                                field("name"),
                                field("description")
                        )
                )
        );

        io.smallrye.graphql.client.Response response = dynamicGraphQLClient.executeSync(query);
        return response.getObject(Product.class, "productById");
    }

}
