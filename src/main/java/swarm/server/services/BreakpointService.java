package swarm.server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Breakpoint;
import swarm.server.domains.Product;
import swarm.server.repositories.BreakpointRepository;
import swarm.server.repositories.ProductRepository;

@Service
@GraphQLApi
public class BreakpointService {
	
	private final BreakpointRepository breakpointRepository;

	@Autowired
	public BreakpointService(BreakpointRepository breakpointRepo) {
		this.breakpointRepository = breakpointRepo;
	}

	@GraphQLQuery(name = "breakpoints")
	public List<Breakpoint> breakpointsBySessionId(@GraphQLArgument(name = "sessionId") Long sessionId){
		return breakpointRepository.findBySessionIdOrderByTimestamp(sessionId);
	}
	
	public Optional<Breakpoint> findById(Long id) {
		return breakpointRepository.findById(id);
	}
	
	@GraphQLMutation(name = "breakpointCreate")
	public Breakpoint breakpointCreate(Breakpoint breakpoint) {
		return breakpointRepository.save(breakpoint);
	}
    
	@GraphQLQuery(name = "breakpoint")
    public Iterable<Breakpoint> breakpointsByTaskId(@GraphQLArgument(name = "taskId") Long taskId){
		return breakpointRepository.findByTaskId(taskId);
    }
}
