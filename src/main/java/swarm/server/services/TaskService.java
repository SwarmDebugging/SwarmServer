package swarm.server.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Breakpoint;
import swarm.server.domains.Method;
import swarm.server.domains.Task;
import swarm.server.domains.Type;
import swarm.server.repositories.BreakpointRepository;
import swarm.server.repositories.MethodRepository;
import swarm.server.repositories.TaskRepository;
import swarm.server.repositories.TypeRepository;

import java.util.Map.Entry;
import java.util.Optional;

@Service
@GraphQLApi
public class TaskService {

	private final TaskRepository taskRepository;
	private final TypeRepository typeRepository;
	private final BreakpointRepository breakpointRepository;
	private final MethodRepository methodRepository;
	
	@Autowired
	public TaskService(TaskRepository taskRepository, TypeRepository typeRepository,
			BreakpointRepository breakpointRepository, MethodRepository methodRepository) {
		this.taskRepository = taskRepository;
		this.typeRepository = typeRepository;
		this.breakpointRepository = breakpointRepository;
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
	
	@GraphQLQuery(name = "getBreakpointGraphData")
	public String getBreakpointGraphData(@GraphQLArgument(name = "taskId") Long taskId) {
		return getBreakpointGraphData(taskId, true);
	}
	
	public String getBreakpointGraphData(Long taskId, boolean closed) {
		Optional<Task> task = taskRepository.findById(taskId);
		if (task == null) {
			return "[]";
		}
		
		StringBuffer graph = new StringBuffer();
		
		if(closed) { 
			graph.append("[");
		}
		
		Map<String, Integer> typeNodes = new HashMap<String,Integer>();  
		List<Type> types = typeRepository.findByTask(task);
		for (Type type : types) {
			if(!typeNodes.containsKey(type.getFullName())) {
				int count = breakpointRepository.countByTaskAndType(task, type.getFullName());
				if(count > 0) {
					typeNodes.put(type.getFullName(),count);
				}
			}
		}
		
		List<String> nodes = sortNodes(typeNodes);
		for (String key : nodes) {
			String label = key.length() < 40 ? key : "..."+ key.substring(key.length() - 40, key.length()); 
			graph.append("{ \"data\": { \"id\": \"T" + key + "\", \"label\": \"" + label + "\", \"shape\": \"roundrectangle\", \"color\": \"#888\"}},");
		}
		
		
//		int r = 255;
//		int g = 255;
//		int b = 255;

		int x = 0;
		int pos = 0;
		

		for (String key : nodes) {
			int y = 0 + pos;
			//pos = pos == 0 ? 10 : 0;
			x += 150;

			List<Breakpoint> breakpoints = breakpointRepository.findByTaskAndType(task, key);
			for(Breakpoint breakpoint : breakpoints) {
				String devName = breakpoint.getType().getSession().getDeveloper().getUsername();

				//int hash = breakpoint.getType().getSession().getDeveloper().getName().hashCode();
				//int r = (hash & 0xFF0000) >> 16;
				//int g = (hash & 0x00FF00) >> 8;
				//int b = hash & 0x0000FF;
				
				
				y += 40;
				graph.append("{ \"data\": { \"id\": \"B" + breakpoint.getId() + "\", \"shape\": \"circle\", \"label\": \"" + (breakpoint.getLineNumber() + 1) + " " + devName +  "\", ");
				//graph.append("\"color\": \"" + String.format("#%02x%02x%02x", r, g, b) + "\"");
				//graph.append("\"color\": \"" + String.format("#%02x%02x%02x", r, g, b) + "\"");
				graph.append("\"dev\": " + breakpoint.getType().getSession().getDeveloper().getId() + ",");
				graph.append("\"color\": \"" + breakpoint.getType().getSession().getDeveloper().getColor() + "\",");
				
				graph.append("\"parent\": \"T" + key + "\"");
				graph.append("},");
				graph.append("\"position\": { \"x\": " + x + ", \"y\": " + y + "} },");
			}
		}

		
		String output;
		if(graph.length() > 2) {
			output =  graph.substring(0, graph.length() - 1) + (closed ? "]" : "");
		} else {
			output = graph.toString() + (closed ? "]" : "");
		}

		return output;
	}

	private List<String> sortNodes(Map<String, Integer> typeNodes) {
		List<String> nodes = new ArrayList<String>();

		Set<Entry<String,Integer>> entrySet = typeNodes.entrySet();
		System.out.println("Entry size " + entrySet.size());
		for (int i = 0; i <= entrySet.size(); i++) {
			String maxKey = "";
			int max = Integer.MIN_VALUE;

			for (Entry<String, Integer> entry : entrySet) {
				if(entry.getValue() > max && !nodes.contains(entry.getKey())) {
					maxKey = entry.getKey();
					max = entry.getValue();
				}
			}
			
			if(!maxKey.equals("") && !nodes.contains(maxKey)) {
				nodes.add(maxKey);
				System.out.println("Adding " + maxKey + " " + max);
			}
		}
		
		System.out.println("Node output size " + nodes.size());
		
		return nodes;
	}
}