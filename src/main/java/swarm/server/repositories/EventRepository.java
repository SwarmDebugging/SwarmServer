package swarm.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import swarm.server.domains.Event;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "events", path = "events")
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("from Event as e where e.session.task.id = :taskId")
    List<Event> findByTask(@Param("taskId") Long taskId);

    @Query("from Event as e where e.session.task.product.id = :productId")
    Iterable<Event> findByProductId(@Param("productId") Long productId);

}