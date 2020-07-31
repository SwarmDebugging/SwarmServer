package swarm.server.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Invocation;
import swarm.server.domains.Method;
import swarm.server.domains.Session;
import swarm.server.domains.Type;
import swarm.server.repositories.InvocationRepository;
import swarm.server.repositories.MethodRepository;
import swarm.server.repositories.SessionRepository;
import swarm.server.repositories.TypeRepository;

@Service
@GraphQLApi
public class SessionService {

	private final SessionRepository sessionRepository; 
	private final TypeRepository  typeRepository; 
	private final MethodRepository  methodRepository; 
	private final InvocationRepository  invocationRepository; 
	private StringBuffer graph;
	
	@Autowired
	public SessionService(SessionRepository sessionRepository, TypeRepository typeRepository, MethodRepository methodRepository, InvocationRepository invocationRepository) {
		this.sessionRepository = sessionRepository;
		this.typeRepository = typeRepository;
		this.methodRepository = methodRepository;
		this.invocationRepository = invocationRepository;
	}
	
	public Optional<Session> sessionById(Long id) {
		return sessionRepository.findById(id);
	}

	@GraphQLQuery(name = "sessions")
	public Iterable<Session> allSessions() {
		return sessionRepository.findAll();
	}
	
	public Iterable<Session> sessionsByTaskId(Long taskId) {
		return sessionRepository.findByTask(taskId);
	}
	
	@GraphQLMutation(name = "sessionCreate")
	public Session save(Session session) {
		return sessionRepository.save(session);
	}
	
	@GraphQLMutation(name = "sessionUpdate") //To update started or finished time of sessions
	public Session updateSession(@GraphQLArgument(name = "id") Long id,
			@GraphQLArgument(name = "started") Date started, 
			@GraphQLArgument(name = "finished") Date finished, 
			@GraphQLArgument(name = "vscodeSession") String vscodeSession) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());
		if(started == null) {
			session.setFinished(finished);
		}else if (finished == null) {
			session.setStarted(started);
		}
		if (vscodeSession != null){
			session.setVscodeSession(vscodeSession);
		}
		return sessionRepository.save(session);
	}

	@GraphQLMutation(name = "sessionStop")
	public Session stopSession(@GraphQLArgument(name = "id") Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		Date date = new Date();
		session.setFinished(date);
		return sessionRepository.save(session);
	}


	@GraphQLMutation(name = "sessionStart") //started and finished necessary?
	public Session sessionStart(Session session) {
		Date date = new Date();
		session.setStarted(new Timestamp(date.getTime()));
		return sessionRepository.save(session);
	}
	
	@GraphQLQuery(name = "sessions")
	public Iterable<Session> sessionsByTaskIdAndDeveloperId(@GraphQLArgument(name = "taskId") Long taskId, @GraphQLArgument(name = "developerId") Long developerId){
    	return sessionRepository.findByTaskAndDeveloper(taskId, developerId);
	}

	@GraphQLQuery(name = "sessions")
	public Iterable<Session> sessionsByDeveloperId(@GraphQLArgument(name = "developerId") Long developerId) {
		return sessionRepository.findByDeveloperId(developerId);
	}
	
	@GraphQLQuery(name = "sessionsVscode")
	public Iterable<Session> sessionsByVscodeSession(@GraphQLArgument(name = "vscodeSession") String vscodeSession){
		return sessionRepository.findByVscode(vscodeSession);
	}

	/*@GraphQLQuery(name = "getGraphData")
    public String getGraphData(@GraphQLArgument(name = "sessionId") Long sessionId, @GraphQLArgument(name = "addType") boolean addType) {
		Optional<Session> session = sessionRepository.findById(sessionId);
		return getGraphData(session, addType, true);
	}
	
	
	public String getGraphData(Session session, boolean addType, boolean closed) {
		graph = new StringBuffer();		
		if(closed) { 
			graph.append("[");
		}
		if(session != null) {
			graphAddSession(session,addType,closed);
			graphAddInvocation(session);
		}
		String output;
		if(graph.length() > 2) {
			output =  graph.substring(0, graph.length() - 1) + (closed ? "]" : "");
		} else {
			output = graph.toString() + (closed ? "]" : "");
		}
		return output;
	}
	
	private void graphAddSession(Optional<Session> session, boolean addType, boolean closed){
		List<Type> types = typeRepository.findBySession(session);
		for (Type type : types) {								
			int hash = type.getFullName().hashCode();
			int r = (hash & 0xFF0000) >> 16;
			int g = (hash & 0x00FF00) >> 8;
			int b = hash & 0x0000FF;			
			boolean newType = true;
			graphAddSessionAddMethod(type,addType,newType,session,r,g,b);
		}
	}
	
	private void graphAddSessionAddMethod(Type type, boolean addType, boolean newType, Optional<Session> session, int r, int g, int b) {
		List<Method> methods = methodRepository.findByType(type);
		for(Method method : methods) {					
			int invocations = invocationRepository.countInvocations(session, method);
			if(invocations > 0) {
				if(addType && newType) {
					newType = false;
					graphAddSessionAddType(type);
				}
				graph.append("{ \"group\": \"nodes\", ");
				graph.append("\"data\": { \"id\": \"M" + method.getId() + "\", \"label\": \"" + type.getName() + ". " + method.getName() + "\", ");
				graph.append("\"color\": \"" + String.format("#%02x%02x%02x", r, g, b) + "\"");				
				if(addType) {
					graph.append(",\"parent\": \"T" + type.getId() + "\"");
				}
				graph.append("}},");
			}
		}
	}
	
	private void graphAddSessionAddType(Type type) {
		graph.append("{ \"group\": \"nodes\", ");
		graph.append("\"data\": { \"id\": \"T" + type.getId() + "\", \"label\": \"" + type.getFullName() + "\", \"shape\": \"roundrectangle\", \"color\": \"#888\"}},");		
	}
	
	private void graphAddInvocation(Optional<Session> session) {
		List<Invocation> invocations = invocationRepository.findBySession(session);
		if(invocations.size() > 0) {
			Map<String,String> labels = new HashMap<String,String>();
			for(int i = 0; i < invocations.size(); i++) {
				Invocation invocation = invocations.get(i);
				String key = invocation.getInvoking().getId() + "->" + invocation.getInvoked().getId(); 
				labels.put(key,(labels.get(key) != null ? labels.get(key) : "") + (i+1) + ",");
			}
			for (Invocation invocation : invocations) {
				labels = graphAddInvocationAddString(invocation,labels);
			}
		}
	}
	
	private Map<String, String> graphAddInvocationAddString(Invocation invocation, Map<String, String> labels) {
		String key = invocation.getInvoking().getId() + "->" + invocation.getInvoked().getId();
		if(labels.containsKey(key)) {
			String label = labels.get(key).substring(0,labels.get(key).length() - 1);					
			graph.append("{ \"group\": \"edges\", ");
			graph.append("\"data\":{ \"id\": \"I" + invocation.getId() + "\", " );
			graph.append("\"source\": " + "\"M" + invocation.getInvoking().getId() + "\", ");
			graph.append("\"target\": " + "\"M" + invocation.getInvoked().getId() + "\", ");
			graph.append("\"line-color\": " + "\"light-gray" + "\", ");
			graph.append("\"target-arrow-color\": " + "\"light-gray" + "\", ");
	        graph.append("\"label\": \"[" + (label.length() > 30 ? "*": label)   + "]\" }},");
			labels.remove(key);
		}
		return labels;
	}
	
	@GraphQLQuery(name = "getStackData")
	public  String getStackData(@GraphQLArgument(name = "sessionId") Long sessionId) {
		StringBuffer graph = new StringBuffer();
	    Optional<Session> session = sessionRepository.findById(sessionId);
				
		List<Method> startingMethods = methodRepository.getStartingMethods(session);
		List<Method> endingMethods = methodRepository.getEndingMethods(session);
		
		graph.append("[");

		int pathIndex = 1;
		List<List<Invocation>> paths = getInvocationPaths(startingMethods, endingMethods,session);
		for (List<Invocation> path : paths) {
			for (Invocation invocation : path) {
				graph.append(addNode(pathIndex,invocation.getInvoking(), invocation.isVirtual()));
				graph.append(addNode(pathIndex,invocation.getInvoked(), false));
				graph.append(addEdge(pathIndex,invocation));
			}
			pathIndex++;
		}
		
		String s = graph.toString();
		if(s.substring(s.length() - 1) == ",") {
			s = s.substring(0,s.length() - 1);
		}
		
		if(graph.length() > 2) {
			return graph.substring(0, graph.length() - 1) + "]";
		} else {
			return graph + "]";
		}
	}
	
	private String addEdge(int path, Invocation invocation) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{ \"group\": \"edges\", ");
		buffer.append("\"data\": { \"id\": \"P" + path + "-I" + invocation.getId() + "\", " );
		buffer.append("\"source\": \"P" + path + "-M" + invocation.getInvoking().getId() + "\", ");
		buffer.append("\"target\": \"P" + path + "-M" + invocation.getInvoked().getId() + "\" , \"label\" : " + invocation.getId() + ", ");
		buffer.append("\"linecolor\": "  + (invocation.isVirtual() ? "\"lightgray" : "\"black")  + "\", ");

		buffer.append("\"style\": \"solid\"}},");
		//buffer.append("\"style\": \"solid\"}},<p>");
		return buffer.toString();
	}
	
	private String addNode(int path, Method method, boolean isVirtual) {
		StringBuffer buffer = new StringBuffer();
		int hash = method.getType().getFullName().hashCode();
		//int hash = method.key.hashCode()
		int r = (hash & 0xFF0000) >> 16;
		int g = (hash & 0x00FF00) >> 8;
		int b = hash & 0x0000FF;
		
		buffer.append("{ \"group\": \"nodes\", ");
		buffer.append("\"data\": { \"id\": \"P" + path + "-M" + method.getId() + "\", \"label\": \"" + method.getType().getName() + " " + method.getName() + "()\", ");
		buffer.append("\"opacity\": \"" + (isVirtual ? 0.2 : 1) + "\", ");
		buffer.append("\"color\": \"" + String.format("#%02x%02x%02x", r, g, b) + "\"");
		
		buffer.append("}},");
		//buffer.append("}},<p>");

		return buffer.toString();
	}
	
	@GraphQLQuery(name = "getInterPathEdges")
	public String getInterPathEdges(@GraphQLArgument(name = "sessionId") Long sessionId) {
		StringBuffer graph = new StringBuffer();
	    Optional<Session> session = sessionRepository.findById(sessionId);
		
		List<Method> startingMethods = methodRepository.getStartingMethods(session);
		List<Method> endingMethods = methodRepository.getEndingMethods(session);
		
		List<List<Invocation>> paths = getInvocationPaths(startingMethods, endingMethods,session);

		graph.append("[");		
		for(int j = 0; j <= paths.size() - 2; j++) {
			List<Invocation> path1 = paths.get(j);
			List<Invocation> path2 = paths.get(j+1);
			int pathIndex1 = j+1;
			int pathIndex2 = j+2;
			
			Invocation connectedInvocation = null;
			
			for(int i = 0; i <= path1.size() - 2; i++) {
				if(i <= path2.size() - 2) {
					Invocation i1 = path1.get(i);
					Invocation i2 = path2.get(i);
					
					if(i1.getId() == i2.getId()) {
						connectedInvocation = i1;
					} else {
						break;
					}
				}
			}
			
			if(connectedInvocation != null) {
				graph.append("{ \"group\": \"edges\", ");
				graph.append("\"data\":{ \"id\": \"IP-" + pathIndex1 +"->"+pathIndex2+ "-I" + connectedInvocation.getId() + "\", " );
				graph.append("\"source\": \"P" + pathIndex1 + "-M" + connectedInvocation.getInvoked().getId() + "\", ");
				graph.append("\"target\": \"P" + pathIndex2 + "-M" + connectedInvocation.getInvoked().getId() + "\" , \"label\" : \"" + connectedInvocation.getId() + "\", ");
				graph.append("\"linecolor\": "  + "\"black" + "\", ");
				graph.append("\"style\": \"dotted\"}},");
				//graph.append("\"style\": \"dotted\"}},<p>");
			}
		}
		
		String s = graph.toString();
		if(s.substring(s.length() - 1) == ",") {
			s = s.substring(0,s.length() - 1);
		}
		
		if(graph.length() > 2) {
			return graph.substring(0, graph.length() - 1) + "]";
		} else {
			return graph + "]";
		}
	}
	
	List<List<Invocation>> getInvocationPaths(List<Method> startingMethods, List<Method> endingMethods, Optional<Session> session) {
		List<List<Invocation>> paths = new ArrayList<List<Invocation>>();
		List<Invocation> uniqueInvocations = new ArrayList<Invocation>();
		
		List<Invocation> sessionInvocations = invocationRepository.findBySession(session); 
				
		for (Invocation sessionInvocation : sessionInvocations) {
			boolean found = false;
			for (Invocation uniqueInvocation : uniqueInvocations) {
				if(sessionInvocation.equals(uniqueInvocation)) {
					found = true;
					break;
				}
			}
			
			if(!found) {
				uniqueInvocations.add(sessionInvocation);
			}
		}
		
		Invocation prevInvocation = null;
		List<Invocation> pathInvocations = new ArrayList<Invocation>(); 
		List<Invocation> stack = new ArrayList<Invocation>();
		boolean firstStack = true;
		
		for (Invocation invocation : uniqueInvocations) {
			if(startingMethods.contains(invocation.getInvoking())) {
				stack = new ArrayList<Invocation>();
			}

			if(prevInvocation != null && prevInvocation.getInvoked().getId() != invocation.getInvoking().getId()) {
				paths.add(pathInvocations);
				
				int i;
				for(i = 0; i <= stack.size() - 1; i++) {
					if(stack.get(i).getInvoking().equals(invocation.getInvoking())) {
						break;
					}
				}

				stack.subList(i,stack.size()).clear();
				
				if(!firstStack) {
					for (Invocation item : stack) {
						item.setVirtual(true);
					}
				} else {
					firstStack = false;
				}
				
				pathInvocations = new ArrayList<Invocation>(stack);

			}
			
			pathInvocations.add(invocation);
			stack.add(invocation);
			prevInvocation = invocation;
		}
		paths.add(pathInvocations);
		return paths;
	}
	
	@GraphQLQuery
	public int countElements(@GraphQLArgument(name = "sessionId") Long sessionId) {
		Optional<Session> session = sessionRepository.findById(sessionId);
		int invocations = invocationRepository.countBySession(session);
		int methods = methodRepository.countBySession(session);
		return invocations + methods;
	}
	
	@GraphQLQuery(name = "getGraphDataByTaskId")
	public String getGraphDataByTaskId(@GraphQLArgument(name = "taskId") Long taskId) {
		String graph = "[";
		
		Iterable<Session> sessions = sessionRepository.findByTask(taskId);
		for (Session session : sessions) {
			graph += getGraphData(session, false, false);
		}
		
		return graph + "]";
	}*/
}