package swarm.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swarm.server.domains.Invocation;
import swarm.server.domains.Type;
import swarm.server.repositories.InvocationRepository;

import java.util.*;

@Service
@GraphQLApi
@RestController
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

	@GraphQLQuery(name = "invocations")
	public Iterable<Invocation> getAllInvocations() {
		return invocationRepository.findAll();
	}

	@RequestMapping("/getInvocationGraph/{taskId}")
	public String getInvocationGraph(@PathVariable Long taskId) {
		StringBuffer html = new StringBuffer();
		html.append(
		"<!DOCTYPE html><html>\n" +
				"<head>\n" +
				"<title>YOYOYO</title>\n" +
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

				Iterable<Invocation> invocations = getInvocationsByTask(taskId);

				Map<String, Type> seenTypes = new HashMap<>();

				for (Invocation invocation: invocations) {
					seenTypes.computeIfAbsent(invocation.getInvoked().getType().getFullName(), k -> invocation.getInvoked().getType());
					seenTypes.computeIfAbsent(invocation.getInvoking().getType().getFullName(), k -> invocation.getInvoking().getType());

					Type invokedType = seenTypes.get(invocation.getInvoked().getType().getFullName());
					Type invokingType = seenTypes.get(invocation.getInvoking().getType().getFullName());
					if(!invokedType.getFullName().equals(invokingType.getFullName())) {
						html.append("{data:{id:'").append(invokedType.getFullName()).append("'}},\n");
						html.append("{data:{id:'").append(invokingType.getFullName()).append("'}},\n");
						html.append("{data:{id:'").append(invokedType.getFullName()).append(",").append(invokingType.getFullName())
								.append("'," + "source: '").append(invokedType.getFullName()).append("',target: '").append(invokingType.getFullName()).append("'}},\n\n");
					}
				}

				html.append("],\n" +
						"        style: [\n" +
						"        \t{\n" +
						"          \tselector: 'node',\n" +
						"            style: {\n" +
						"            \tshape: 'hexagon',\n" +
						"              'background-color': 'red',\n" +
						"              label: 'data(id)'\n" +
						"            }\n" +
						"          },\n" +
						"          {\n" +
						"          \tselector: 'edge',\n" +
						"            style: {\n" +
						"            \t'target-arrow-color': '#ccc',\n" +
						"              'target-arrow-shape': 'triangle',\n" +
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

}