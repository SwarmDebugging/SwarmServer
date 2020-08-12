package swarm.server.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Method;
import swarm.server.domains.Task;
import swarm.server.repositories.MethodRepository;
import swarm.server.repositories.TaskRepository;

import java.util.Optional;

@Service
@GraphQLApi
public class TaskService {

	private final TaskRepository taskRepository;
	private final MethodRepository methodRepository;
	
	@Autowired
	public TaskService(TaskRepository taskRepository, MethodRepository methodRepository) {
		this.taskRepository = taskRepository;
		this.methodRepository = methodRepository;
	}

	@GraphQLQuery(name = "tasks") 
	public Iterable<Task> taskByProductId(@GraphQLArgument(name = "productId") Long productId) {
		return taskRepository.findByProductId(productId);
	}

	@GraphQLMutation(name = "taskDone")
	public Task taskDone(@GraphQLArgument(name = "taskId") long taskId) {
		Optional<Task> task = taskRepository.findById(taskId);

		task.get().setDone(true);
		return taskRepository.save(task.get());
	}

	public Task save(Task task) {
		return taskRepository.save(task);
	}
	
	public Optional<Task> taskById(Long id) {
		return taskRepository.findById(id);
	}

	@GraphQLMutation(name = "taskCreate")
	public Task taskCreate(Task task) {
		return taskRepository.save(task);
	}

	@GraphQLMutation(name = "taskUpdate")
	public Task taskUpdateTitle(@GraphQLArgument(name = "taskId") Long taskId, @GraphQLArgument(name = "title") String title) {
		Task task = taskRepository.findById(taskId).orElse(null);
		if(task != null) {
			task.setTitle(title);
			return taskRepository.save(task);
		}
		return null;
	}

	@GraphQLQuery(name = "tasks")
	public Iterable<Task> TasksByProductId(@GraphQLArgument(name = "productId") Long productId) {
		return taskRepository.findActiveTasksByProductId(productId);
	}

	@GraphQLQuery(name = "methodsUsedInTask")
	public Iterable<Method> getMethodsUsedTask(@GraphQLArgument(name = "taskId") Long taskId) {
		List<Method> methodsUsedInPreviousSessions = methodRepository.getMethodsUsedInPreviousSessions(taskId);
		return methodsUsedInPreviousSessions;

	}
	
	@GraphQLQuery(name = "tasks")
    public Iterable<Task> allTasks() {
        return taskRepository.findAll();
    }
	
	@GraphQLQuery(name = "tasks")
	public Iterable<Task> TasksByDeveloperId(@GraphQLArgument(name = "developerId") Long developerId) {
		return taskRepository.findByDeveloperId(developerId);
	}

	@GraphQLQuery(name = "tasksActive")
	public List<Task> activeTasks(@GraphQLArgument(name = "developerId") Long developerId, @GraphQLArgument(name = "productId") Long productId) {

		if(developerId != null){
			return taskRepository.findActiveTasksByDeveloperId(developerId);
		} else if(productId != null) {
			return taskRepository.findActiveTasksByProductId(productId);
		} else {
			return taskRepository.findActiveTasksByDeveloperIdAndProductId(developerId, productId);
		}
	}
}