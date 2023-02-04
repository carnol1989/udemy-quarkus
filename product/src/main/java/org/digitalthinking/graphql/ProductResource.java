package org.digitalthinking.graphql;

import org.digitalthinking.entites.Product;
import org.digitalthinking.repositories.ProductRepository;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.inject.Inject;
import java.util.List;

@GraphQLApi
public class ProductResource {

    @Inject
    ProductRepository repository;

    @Query
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    @Query
    public Product getProductById(@Name("productId") Long id) {
        return repository.findById(id).get();
    }

}
