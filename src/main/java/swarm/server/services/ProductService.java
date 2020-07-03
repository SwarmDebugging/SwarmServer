package swarm.server.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Invocation;
import swarm.server.domains.Product;
import swarm.server.repositories.InvocationRepository;
import swarm.server.repositories.ProductRepository;
import swarm.server.repositories.TypeRepository;

@GraphQLApi
@Service
public class ProductService {
	
	private final ProductRepository productRepository;
	private final InvocationRepository invocationRepository;
	private final TypeRepository typeRepository;
	
	@Autowired
	public ProductService(ProductRepository productRepository, InvocationRepository invocationRepository, TypeRepository typeRepository) {
		this.productRepository = productRepository;
		this.invocationRepository = invocationRepository;
		this.typeRepository = typeRepository;
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
	
	@GraphQLQuery(name = "getProductPaths")
	public String getProductPaths(@GraphQLArgument(name = "productId") Long productId) {
		StringBuffer graph = new StringBuffer("[");

		Optional<Product> product = productRepository.findById(productId);
		
		if(product != null) {
			List<Invocation> invocations = invocationRepository.findByProduct(product);

			List<String> names = typeRepository.findFullNamesByProduct(product);
			for (String name : names) {
				boolean found = false;
				for (Invocation invocation : invocations) {
					String f1 = invocation.getInvoking().getType().getFullName();
					String f2 = invocation.getInvoked().getType().getFullName();
					
					if(f1.equals(name) || f2.equals(name)) {
						found = true;
						break;
					}
				}
				
				if(found) {
					String label = name.length() < 40 ? name : "..."+ name.substring(name.length() - 40, name.length()); 
					graph.append("{ \"data\": { \"id\": \"T" + name + "\", \"label\": \"" + label + "\", \"shape\": \"roundrectangle\", \"color\": \"#888\"}},");
					found = false;
				}
			}
			
			Map<String,Integer> countI = new HashMap<String, Integer>();
			if(invocations.size() > 0) {
				Map<String,String> labels = new HashMap<String,String>();
				for(int i = 0; i < invocations.size(); i++) {
					Invocation invocation = invocations.get(i);
					String key = invocation.getSession().getTask().getId() + "-" + invocation.getInvoking().getType().getFullName() + "->" + invocation.getInvoked().getType().getFullName(); 
					labels.put(key,(labels.get(key) != null ? labels.get(key) : "") + (i+1) + ",");
				}

				for (Invocation invocation : invocations) {
					String key = invocation.getSession().getTask().getId() + "-" + invocation.getInvoking().getType().getFullName() + "->" + invocation.getInvoked().getType().getFullName();
					countI.put(key, countI.get(key) !=  null ? countI.get(key) + 1 : 1);
				}
				
				for (Invocation invocation : invocations) {
					if(!invocation.getInvoking().getType().getFullName().equals(invocation.getInvoked().getType().getFullName())) {
						String key = invocation.getSession().getTask().getId() + "-" + invocation.getInvoking().getType().getFullName() + "->" + invocation.getInvoked().getType().getFullName();
						if(labels.containsKey(key)) {
							String label = labels.get(key).substring(0,labels.get(key).length() - 1);

							graph.append("{ \"group\": \"edges\", ");
							graph.append("\"data\":{ \"id\": \"I" + invocation.getId() + "\", " );
							graph.append("\"source\": " + "\"T" + invocation.getInvoking().getType().getFullName() + "\", ");
							graph.append("\"target\": " + "\"T" + invocation.getInvoked().getType().getFullName() + "\", ");
							//graph.append("\"width\": " + (Double.valueOf(Math.sqrt(countI.get(key))).intValue()+1) + ", ");
							graph.append("\"width\": " + countI.get(key) + ", ");
							//graph.append("\"width\": " + 1 + ", ");
							graph.append("\"task\": " + invocation.getInvoking().getType().getSession().getTask().getId() + ", ");
							graph.append("\"dev\": " + invocation.getInvoking().getType().getSession().getDeveloper().getId() + ", ");
							graph.append("\"taskColor\": \"" + invocation.getInvoking().getType().getSession().getTask().getColor() + "\", ");
							graph.append("\"devColor\": \"" + invocation.getInvoking().getType().getSession().getDeveloper().getColor() + "\", ");
					        graph.append("\"label\": \"[" + (label.length() > 30 ? "*": label)   + "]\" }},");
							labels.remove(key);
						}
					}
				}
			}
		}

		String output = "";
		if(graph.length() > 2) {
			output =  graph.substring(0, graph.length() - 1);
		}

		output = output + "]";

		return output;	
	}
}