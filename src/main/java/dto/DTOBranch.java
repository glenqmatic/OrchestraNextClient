package dto;

public class DTOBranch {
	@Override
	public String toString() {
		return name;
	}

	String name;
	int id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getIdAsString() {
		return String.valueOf(id);
	}

	public void setId(int id) {
		this.id = id;
	}
}
