package swarm.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import swarm.server.domains.Session;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "sessions", path = "sessions")
public interface SessionRepository extends JpaRepository<Session, Long> {

	@Query("Select s from Session s Where task.id = :taskId and developer.id = :developerId ")
	Iterable<Session> findByTaskAndDeveloper(@Param("taskId") Long taskId, @Param("developerId") Long developerId);
	
	@Query("Select s from Session s Where task.id = :taskId")
	Iterable<Session> findByTask(@Param("taskId") Long taskId);

	@Query("Select s from Session s Where vscodeSession = :vscodesession")
	Iterable<Session> findByVscode(@Param("vscodesession") String vscodeSession);

	Iterable<Session> findByDeveloperId(Long Id);
}
