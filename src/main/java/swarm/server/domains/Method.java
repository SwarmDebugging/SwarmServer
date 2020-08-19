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
public class Method implements Serializable{

	private static final long serialVersionUID = 5499015545204459102L;

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
	
	@ManyToOne(optional = false)
	private Type type;

	@Column(nullable = true)
	String key;

	@Column(nullable = false)
	String name;

	@Column(nullable = true)
	String signature;
	
	@Column(name="CREATION_TS", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable=false, updatable=false)
	private Calendar timestamp;
	
	public Method () {}
	
	public Method(Type type, String key, String name, String signature) {
		this.type = type;
		this.key = key;
		this.name = name;
		this.signature = signature;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Method method = (Method) o;

        return id.equals(method.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
	
    @Override
	public String toString() {
		return type.getFullName() +"." +  this.name;
	}
}
