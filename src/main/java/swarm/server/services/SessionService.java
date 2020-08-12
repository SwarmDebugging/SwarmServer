package swarm.server.services;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Session;
import swarm.server.repositories.SessionRepository;

@Service
@GraphQLApi
public class SessionService {

	private final SessionRepository sessionRepository;

	@Autowired
	public SessionService(SessionRepository sessionRepository) {
		this.sessionRepository = sessionRepository;
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
	
	@GraphQLMutation(name = "sessionUpdate")
	public Session updateSession(@GraphQLArgument(name = "id") Long id,
			@GraphQLArgument(name = "started") Date started, 
			@GraphQLArgument(name = "finished") Date finished, 
			@GraphQLArgument(name = "vscodeSession") String vscodeSession) {
		Session session = sessionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
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


	@GraphQLMutation(name = "sessionStart")
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
}