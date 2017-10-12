package org.swarmdebugging.server.repository.search;

import org.swarmdebugging.server.domain.Event;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Event entity.
 */
public interface EventSearchRepository extends ElasticsearchRepository<Event, Long> {
}