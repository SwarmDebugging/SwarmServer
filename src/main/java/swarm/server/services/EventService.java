package swarm.server.services;

import io.leangen.graphql.annotations.GraphQLArgument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swarm.server.domains.Event;
import swarm.server.domains.Invocation;
import swarm.server.domains.Type;
import swarm.server.repositories.EventRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@GraphQLApi
@RestController
public class EventService {
	
	private final EventRepository eventRepository;
	private final InvocationService invocationService;
	private final TaskService taskService;
	private Map<String, Integer> numberOfEdgesBetween2Types;
	private Map<String, Integer> numberOfEventsOnTypes;
	
	@Autowired
	public EventService(EventRepository eventRepository, InvocationService invocationService,
						TaskService taskService) {
		this.eventRepository = eventRepository;
		this.invocationService = invocationService;
		this.taskService = taskService;
	}
	
	public Event save(Event event) {
		return eventRepository.save(event);
	}

	@GraphQLQuery(name = "events")
	public Iterable<Event> getAllEvents() {
		return eventRepository.findAll();
	}
	
	@GraphQLMutation(name = "eventCreate")
	public Event createEvent(Event event) {
		return eventRepository.save(event);
	}

	@GraphQLQuery(name = "events")
	public Iterable<Event> getEventByTask(@GraphQLArgument(name = "taskId")Long taskId){
		return eventRepository.findByTask(taskId);
	}

	public Iterable<Event> eventsByProduct(Long productId) {
		return eventRepository.findByProductId(productId);
	}

	@RequestMapping("/getProductInvocationGraph/{productId}/{constantNodeSize}")
	public String getProductInvocationGraph(@PathVariable Long productId, @PathVariable boolean constantNodeSize) {
		StringBuilder html = new StringBuilder();
		html.append(
				"<!DOCTYPE html><html>\n" +
						"<head>\n" +
						"<title>ProductInvocationGraph</title>\n" +
						"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.15.2/cytoscape.min.js\"></script>\n" +
						"</head>\n" +
						"\n" +
						"<style>\n" +
						"  #cy {\n" +
						"    width: 100%;\n" +
						"    height: 100%;\n" +
						"    position: absolute;\n" +
						"    top: 0px;\n" +
						"    left: 0px;\n" +
						"  }\n" +
						"</style>\n" +
						"\n" +
						"<body>\n" +
						"  <div id=\"cy\"></div>\n" +
						"  <script>\n" +
						"    var cy = cytoscape({\n" +
						"      container: document.getElementById('cy'),\n" +
						"      elements: [\n" );

		String graphData = generateProductGraphData(productId, constantNodeSize);
		html.append(graphData);

		html.append("],\n" +
				"        style: [\n" +
				"        \t{\n" +
				"          \tselector: 'node',\n" +
				"            style: {\n" +
				"            \tshape: 'rectangle',\n" +
				"              'background-color': 'red',\n" +
				"              label: 'data(id)',\n" +
				"              'width': 'data(mySize)',\n" +
				"              'height': 'data(mySize)'\n" +
				"            }\n" +
				"          },\n" +
				"          {\n" +
				"          \tselector: 'edge',\n" +
				"            style: {\n" +
				"            \t'target-arrow-color': 'red',\n" +
				"              'target-arrow-shape': 'triangle',\n" +
				"              'width': 'data(myWidth)',\n " +
				"              'curve-style': 'bezier'\n" +
				"            }\n" +
				"          }\n" +
				"        ],\n" +
				"        layout: {\n" +
				"        \tname: 'circle'\n" +
				"        }\n" +
				"    });\n" +
				"  </script>\n" +
				"</body>\n" +
				"</html>");

		return html.toString();
	}

	private String generateProductGraphData(Long productId, boolean constantNodeSize) {
		StringBuilder graphData = new StringBuilder();
		numberOfEdgesBetween2Types = new HashMap<>();
		numberOfEventsOnTypes = new HashMap<>();

		Iterable<Event> events = eventsByProduct(productId);
		Iterable<Invocation> invocations = invocationService.getInvocationsByProduct(productId);

		addEventsToMaps(events);
		addInvocationToMaps(invocations);

		graphData.append(generateGraphDataStringFromMaps(constantNodeSize));

		return graphData.toString();
	}

	@RequestMapping("/getTaskInvocationGraph/{taskId}/{constantNodeSize}")
	public String getTaskInvocationGraph(@PathVariable Long taskId, @PathVariable boolean constantNodeSize) {
		StringBuilder html = new StringBuilder();
		html.append(
				"<!DOCTYPE html><html>\n" +
						"<head>\n" +
						"<title>TaskInvocationGraph</title>\n" +
						"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.15.2/cytoscape.min.js\"></script>\n" +
						"</head>\n" +
						"\n" +
						"<style>\n" +
						"  #cy {\n" +
						"    width: 100%;\n" +
						"    height: 100%;\n" +
						"    position: absolute;\n" +
						"    top: 0px;\n" +
						"    left: 0px;\n" +
						"  }\n" +
						"</style>\n" +
						"\n" +
						"<body>\n" +
						"  <div id=\"cy\"></div>\n" +
						"  <script>\n" +
						"    var cy = cytoscape({\n" +
						"      container: document.getElementById('cy'),\n" +
						"      elements: [\n" );

		String graphData = generateTaskGraphData(taskId, constantNodeSize);
		html.append(graphData);

		html.append("],\n" +
				"        style: [\n" +
				"        \t{\n" +
				"          \tselector: 'node',\n" +
				"            style: {\n" +
				"            \tshape: 'rectangle',\n" +
				"              'background-color': 'red',\n" +
				"              label: 'data(id)',\n" +
				"              'width': 'data(mySize)',\n" +
				"              'height': 'data(mySize)'\n" +
				"            }\n" +
				"          },\n" +
				"          {\n" +
				"          \tselector: 'edge',\n" +
				"            style: {\n" +
				"            \t'target-arrow-color': 'red',\n" +
				"              'target-arrow-shape': 'triangle',\n" +
				"              'width': 'data(myWidth)',\n " +
				"              'curve-style': 'bezier'\n" +
				"            }\n" +
				"          }\n" +
				"        ],\n" +
				"        layout: {\n" +
				"        \tname: 'circle'\n" +
				"        }\n" +
				"    });\n" +
				"  </script>\n" +
				"</body>\n" +
				"</html>");

		return html.toString();
	}

	private String generateTaskGraphData(Long taskId, boolean constantNodeSize) {
		StringBuilder graphData = new StringBuilder();
		numberOfEdgesBetween2Types = new HashMap<>();
		numberOfEventsOnTypes = new HashMap<>();

		Iterable<Event> events = getEventByTask(taskId);
		Iterable<Invocation> invocations = invocationService.getInvocationsByTask(taskId);

		addEventsToMaps(events);
		addInvocationToMaps(invocations);

		graphData.append(generateGraphDataStringFromMaps(constantNodeSize));

		return graphData.toString();
	}

	private void addEventsToMaps(Iterable<Event> events) {
		Type lastSeenType  = null;

		for (Event event: events) {
			Type type = event.getMethod().getType();

			if(event.getKind().equals("Resume") || event.getKind().equals("StepInto")) {
				lastSeenType = null;
			} else if(event.getKind().equals("Breakpoint Added") || event.getKind().equals("Breakpoint Removed")) {
				continue;
			} else {
				if (lastSeenType != null && !type.getFullName().equals(lastSeenType.getFullName())) {
					String edge = lastSeenType.getFullName() + "," + type.getFullName();
					Integer j = numberOfEdgesBetween2Types.get(edge);
					numberOfEdgesBetween2Types.put(edge, (j == null) ? 1 : j + 1);
				}
				lastSeenType = type;
				Integer x = numberOfEventsOnTypes.get(type.getFullName());
				numberOfEventsOnTypes.put(type.getFullName(), (x == null) ? 1 : x + 1);
			}
		}
	}

	private void addInvocationToMaps(Iterable<Invocation> invocations) {
		for (Invocation invocation: invocations) {
			Type invokedType = invocation.getInvoked().getType();
			Type invokingType = invocation.getInvoking().getType();
			Integer x = numberOfEventsOnTypes.get(invokedType.getFullName());
			numberOfEventsOnTypes.put(invokedType.getFullName(), (x == null) ? 1 : x + 1);
			Integer y = numberOfEventsOnTypes.get(invokingType.getFullName());
			numberOfEventsOnTypes.put(invokingType.getFullName(), (y == null) ? 1 : y + 1);

			if(!invokedType.getFullName().equals(invokingType.getFullName())) {
				String edge = invokingType.getFullName() + "," + invokedType.getFullName();
				Integer j = numberOfEdgesBetween2Types.get(edge);
				numberOfEdgesBetween2Types.put(edge, (j == null) ? 1 : j + 1);
			}
		}
	}

	private String generateGraphDataStringFromMaps(boolean constantNodeSize) {
		return generateNodeGraphDataString(constantNodeSize) +
				generateEdgeGraphDataString();
	}

	private String generateNodeGraphDataString(boolean constantNodeSize) {
		StringBuilder graphData = new StringBuilder();

		for (Map.Entry<String, Integer> stringIntegerEntry: numberOfEventsOnTypes.entrySet()) {
			Map.Entry element = stringIntegerEntry;
			if(constantNodeSize) {
				graphData.append("{data:{id:'").append(element.getKey()).append("',mySize: ")
						.append(10).append("}},\n");
			} else {
				graphData.append("{data:{id:'").append(element.getKey()).append("',mySize: ")
						.append(element.getValue()).append("}},\n");
			}
		}
		return graphData.toString();
	}

	private String generateEdgeGraphDataString() {
		StringBuilder graphData = new StringBuilder();

		for (Map.Entry<String, Integer> stringIntegerEntry : numberOfEdgesBetween2Types.entrySet()) {
			Map.Entry element = stringIntegerEntry;
			String[] typeNames = element.getKey().toString().split(",");
			graphData.append("{data:{id:'").append(typeNames[0]).append(",").append(typeNames[1])
					.append("'," + "target: '").append(typeNames[1]).append("',source: '").append(typeNames[0]).append("',myWidth: ")
					.append(element.getValue()).append("}},\n\n");
		}
		return graphData.toString();
	}
}