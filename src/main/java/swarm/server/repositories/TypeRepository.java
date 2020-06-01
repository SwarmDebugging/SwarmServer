package swarm.server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import swarm.server.domains.Product;
import swarm.server.domains.Session;
import swarm.server.domains.Task;
import swarm.server.domains.Type;

@RepositoryRestResource(collectionResourceRel = "types", path = "types")
public interface TypeRepository extends JpaRepository<Type, Long> {
	
	@Query("Select t from Type t Where t.session.id = :sessionId")
	List<Type> findBySessionId(@Param("sessionId") Long sessionId);

	@Query("Select t from Type t Where t.session.id = :sessionId and t.fullName = :fullName and t.artefact.sourceCode = :sourceCode")
	List<Type> findBySessionIdFullNameAndSourceCode(@Param("sessionId") Long sessionId, @Param("fullName") String fullName, @Param("sourceCode") String sourceCode);

	List<Type> findBySession(@Param("session") Optional<Session> session);
	
	@Query("Select t from Type t Where t.session.task = :task order by t.fullName")
	List<Type> findByTask(@Param("task") Optional<Task> task);

	@Query("Select t.fullName from Type t Where t.session.task.product = :product group by fullName order by t.fullName")
	List<String> findFullNamesByProduct(@Param("product") Optional<Product> product);
	
	@Query("Select t from Type t, Method m where m.type.id = t.id and :methodId = m.id")
	Type findByMethodId(@Param("methodId") Long methodId);
}