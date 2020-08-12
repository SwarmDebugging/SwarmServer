package swarm.server.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import swarm.server.domains.Invocation;
import swarm.server.domains.Method;
import swarm.server.domains.Product;
import swarm.server.domains.Session;
import swarm.server.domains.Task;

@RepositoryRestResource(collectionResourceRel = "invocations", path = "invocations")
public interface InvocationRepository extends JpaRepository<Invocation, Long> {
	
	@Query("Select i from Invocation i Where i.session.id = :sessionId and i.invoking.id = :invokingId and i.invoked.id = :invokedId")
	List<Invocation> findByMethods(@Param("sessionId") Long sessionId, @Param("invokingId") Long invokingId, @Param("invokedId") Long invokedId);

	@Query("Select count(i) from Invocation i where i.session = :session and (i.invoking = :method or i.invoked = :method)")
	int countInvocations(@Param("session") Optional<Session> session, @Param("method") Method method);

	@Query("from Invocation as i where i.session = :session order by i.id")
	List<Invocation> findByTask(@Param("session") Optional<Session> session);

	int countBySession(Optional<Session> session);

	@Query("from Invocation as i where i.session.task.id = :taskId order by i.id")
	List<Invocation> findByTask(@Param("taskId") Long taskId);
	
	@Query("from Invocation as i Where i.session.task.product = :product")
	List<Invocation> findByProduct(@Param("product") Optional<Product> product);
}