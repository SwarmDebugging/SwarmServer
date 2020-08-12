package swarm.server.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Developer;
import swarm.server.domains.Task;
import swarm.server.repositories.DeveloperRepository;
import swarm.server.repositories.TaskRepository;

@Service
@GraphQLApi
public class DeveloperService {

	private final DeveloperRepository developerRepository; 
	private final TaskRepository taskRepository;

	@Autowired
	public DeveloperService(DeveloperRepository developerRepository, TaskRepository taskRepository) {
		this.developerRepository = developerRepository;
		this.taskRepository = taskRepository;
	}
	
	public Optional<Developer> developerById(Long id) {
		return developerRepository.findById(id);
	}
	
	@GraphQLQuery(name = "developer")
	public Developer developerByUsername(@GraphQLArgument(name = "username") String username) {
		return developerRepository.findByUsernameAllIgnoringCase(username);
	}

	@GraphQLQuery(name = "login")
	public Iterable<Task> login(@GraphQLArgument(name = "username") String username) {
		Developer developer = developerRepository.findByUsernameAllIgnoringCase(username);
		developer.setLogged(true);
		developerRepository.save(developer);
		return taskRepository.findByDeveloperId(developer.getId());
	}
	
	@GraphQLQuery(name = "developers")
	public Iterable<Developer> allDevelopers() {
		return developerRepository.findAll();
	}
	
	public Developer save(Developer developer) {
		return developerRepository.save(developer);
	}
	
	@GraphQLMutation(name = "developerCreate")
	public Developer createDeveloper(Developer developer) {
		developer.setColor("color");
		return developerRepository.save(developer);
	}
}
