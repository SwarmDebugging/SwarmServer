package swarm.server.domains;

public class TypeWrapper { //For REST request to create a new type

	private Type type;
	
	private String source;
		
	public TypeWrapper(Type type, String source) {
		this.type = type;
		this.source = source;
	}
		
	public TypeWrapper() {}
		
	public Type getType() {
			return type;
	}

	public void setType(Type type) {
			this.type = type;
	}

	public String getSource() {
			return source;
	}

	public void setSource(String source) {
			this.source = source;
	}
	
	public int hashCode() {
		int hash = 7;
    	hash = 31 * hash + (type.getFullName() == null ? 0 : type.getFullName().hashCode());
    	hash = 31 * hash + (source == null ? 0 : source.hashCode());
    	return hash;
    }
}
