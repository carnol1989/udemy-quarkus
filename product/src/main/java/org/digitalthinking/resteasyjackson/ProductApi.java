package org.digitalthinking.resteasyjackson;

import org.digitalthinking.entites.Product;
import org.digitalthinking.repositories.ProductRepository;

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
import java.util.List;

@Path("/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductApi {
 @Inject
    ProductRepository pr;

    @GET
    public List<Product> list() {
        return pr.listProduct();
    }

    @GET
    @Path("/{id}")
    public Product findProductById(@PathParam("id") Long id) {
        return pr.getProductById(id);
    }

    @POST
    public Response add(Product p) {
        pr.createdProduct(p);
        return Response.ok().build();
    }

    @PUT
    public Response update(Product p) {
        Product product = pr.getProductById(p.getId());
        product.setCode(p.getCode());
        product.setName(p.getName());
        product.setDescription(p.getDescription());
        pr.updateProduct(product);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{Id}")
    public Response delete(@PathParam("Id") Long Id) {
       pr.deleteProduct(pr.getProductById(Id));
       return Response.ok().build();
    }

}
