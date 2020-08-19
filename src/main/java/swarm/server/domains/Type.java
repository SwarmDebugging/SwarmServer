package swarm.server.domains;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Entity
public class Type implements Serializable{

	private static final long serialVersionUID = -7812590943533400550L;

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
	
	@ManyToOne(optional = true)
	private Namespace namespace;
	
	@ManyToOne(optional = false)
	private Session session;

	@Column(nullable = false)
	String fullName;
	
	@Column(nullable = false)
	String fullPath;
	
	@Column(nullable = false)
	String name;
	
	@ManyToOne
	private Artefact artefact;

	@Column(name="CREATION_TS", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable=false, updatable=false)
	private Calendar timestamp;
	
	public Type () {}
	
	public Type(Namespace namespace, Session session, String fullName, String fullPath, String name, Artefact artefact) {
		this.namespace = namespace;
		this.session = session;
		this.fullName = fullName;
		this.fullPath = fullPath;
		this.name = name;
		this.artefact = artefact;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public void setNamespace(Namespace namespace) {
		this.namespace = namespace;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Artefact getArtefact() {
		return artefact;
	}

	public void setArtefact(Artefact artefact) {
		this.artefact = artefact;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Type type = (Type) o;

        return id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
		return id + ": " + fullName;
	}
}
