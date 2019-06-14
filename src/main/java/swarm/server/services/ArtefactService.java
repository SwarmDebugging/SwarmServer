package swarm.server.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import swarm.server.domains.Artefact;
import swarm.server.repositories.ArtefactRepository;

@GraphQLApi
@Service
public class ArtefactService {
	
	private final ArtefactRepository artefactRepository;
	
	@Autowired
	public ArtefactService(ArtefactRepository artefactRepository) {
		this.artefactRepository = artefactRepository;
	}
	
	@GraphQLQuery
	public Iterable<Artefact> allArtefacts() {
        return artefactRepository.findAll();
    }
	
	@GraphQLQuery
	public Optional<Artefact> artefactById(@GraphQLArgument(name = "artefactId") Long artefactId) {
		return artefactRepository.findById(artefactId);
	}
}