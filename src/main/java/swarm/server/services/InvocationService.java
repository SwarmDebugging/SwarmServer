package swarm.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Invocation;
import swarm.server.repositories.InvocationRepository;

@Service
@GraphQLApi
public class InvocationService {

	private final InvocationRepository invocationRepository;
	
	@Autowired
	public InvocationService(InvocationRepository invocationRepository) {
		this.invocationRepository = invocationRepository;
	}
	
	public Invocation save(Invocation invocation) {
		return invocationRepository.save(invocation);
	}
	
	@GraphQLMutation(name = "invocationCreate")
	public Invocation createInvocation(Invocation invocation) {
		return invocationRepository.save(invocation);
	}

	@GraphQLQuery(name="invocations")
	public Iterable<Invocation> getInvocationsByMethods(
			@GraphQLArgument(name = "sessionId") Long sessionId, 
			@GraphQLArgument(name = "invokingId") Long invokingId, 
			@GraphQLArgument(name = "invokedId") Long invokedId) {
		return invocationRepository.findByMethods(sessionId, invokingId, invokedId);
	}

	@GraphQLQuery(name = "invocations")
	public Iterable<Invocation> getInvocationsByTask(@GraphQLArgument(name = "taskId") Long taskId) {
		return invocationRepository.findByTask(taskId);
	}

	public Iterable<Invocation> getInvocationsByProduct(Long productId) {
		return invocationRepository.findByProductId(productId);
	}

	@GraphQLQuery(name = "invocations")
	public Iterable<Invocation> getAllInvocations() {
		return invocationRepository.findAll();
	}

}