package swarm.server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Method;
import swarm.server.domains.Session;
import swarm.server.repositories.MethodRepository;
import swarm.server.repositories.SessionRepository;

@Service
@GraphQLApi
public class MethodService {

	private final MethodRepository methodRepository; 
	private final SessionRepository sessionRepository;
	
	@Autowired
	public MethodService(MethodRepository methodRepository, SessionRepository sessionRepository) {
		this.methodRepository = methodRepository;
		this.sessionRepository = sessionRepository;
	}
	
	public Optional<Method> methodById(Long id) {
		return methodRepository.findById(id);
	}
	
	public Method save(Method method) {
		return methodRepository.save(method);
	}

	@GraphQLMutation(name = "methodCreate")
	public Method methodCreate(Method method) {
		List<Method> foundMethod = methodRepository.findByTypeIdSignatureAndName(method.getType().getId(), method.getSignature(), method.getName());
		if(!foundMethod.isEmpty()) {
			return foundMethod.get(0);
		}

		return methodRepository.save(method);
	}

	@GraphQLQuery(name = "methods")
	public Iterable<Method> methodByTypeId(@GraphQLArgument(name = "typeId") Long typeId) {
		return methodRepository.findByTypeId(typeId);
	}
	
	@GraphQLQuery(name = "startingMethods")
    public Iterable<Method> startingMethodsBySessionId(@GraphQLArgument(name = "sessionId") Long sessionId){
    	Optional<Session> session = sessionRepository.findById(sessionId);
    	return methodRepository.getStartingMethods(session);
    }
    
	@GraphQLQuery(name = "endingMethods")
    public Iterable<Method> endingMethodsBySessionId(@GraphQLArgument(name = "sessionId") Long sessionId){
		Optional<Session> session = sessionRepository.findById(sessionId);
    	return methodRepository.getEndingMethods(session);
    }
}