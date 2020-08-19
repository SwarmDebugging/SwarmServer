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
public class Event implements Serializable{

	private static final long serialVersionUID = 4778378924903070737L;

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
	
	@ManyToOne(optional = false)
	private Method method;

	@ManyToOne(optional = false)
	private Session session;

	String charStart;

	String charEnd;

	Integer lineNumber;

	String detail;

	String kind;
	
	@Column(name="CREATION_TS", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable=false, updatable=false)
	private Calendar timestamp;
	
	public Event () {}
	
	public Event(Method method, Session session, String charStart, String charEnd, Integer lineNumber, String detail, String kind) {
		this.method = method;
		this.session = session;
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.lineNumber = lineNumber;
		this.detail = detail;
		this.kind = kind;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getCharStart() {
		return charStart;
	}

	public void setCharStart(String charStart) {
		this.charStart = charStart;
	}

	public String getCharEnd() {
		return charEnd;
	}

	public void setCharEnd(String charEnd) {
		this.charEnd = charEnd;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return id.equals(event.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
		return id + ": " + detail;
	}
}
