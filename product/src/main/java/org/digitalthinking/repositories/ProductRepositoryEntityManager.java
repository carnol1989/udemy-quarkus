package org.digitalthinking.repositories;

import org.digitalthinking.entites.Product;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ProductRepositoryEntityManager {
    @Inject
    EntityManager em;

    @Transactional
    public void createdProduct(Product p){
        em.persist(p);
    }

    @Transactional
    public void updateProduct(Product p) {
        em.merge(p);
    }

    @Transactional
    public void deleteProduct(Product p){
        em.remove(p);
    }

    @Transactional
    public Product getProductById(Long productId) {
        Product product = em.find(Product.class, productId);
        return product;
    }

    @Transactional
    public List<Product> listProduct(){
        List<Product> products = em.createQuery("select p from Product p").getResultList();
        return products;
    }
}
