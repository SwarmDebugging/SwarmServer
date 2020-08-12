package swarm.server.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Product;
import swarm.server.repositories.ProductRepository;

@GraphQLApi
@Service
public class ProductService {
	
	private final ProductRepository productRepository;
	
	@Autowired
	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@GraphQLQuery(name = "allProducts")
	public Iterable<Product> allProduct() {
		return productRepository.findAll();
	}
	
	public Optional<Product> productById(Long id) {
		return productRepository.findById(id);
	}
	
	public Product save(Product product) {
		return productRepository.save(product);
	}

	@GraphQLQuery(name = "products")
	public Iterable<Product> products(Long developerId) {
		return productRepository.findByDeveloperId(developerId);
	}
	
	@GraphQLMutation(name = "productCreate")
	public Product createProduct(Product product) {
		return productRepository.save(product);
	}
}