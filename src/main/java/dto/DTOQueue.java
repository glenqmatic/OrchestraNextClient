package dto;

public class DTOQueue implements OrchestraDTO{
	private int id;
	private String name;
	private int customersWaiting;
	private int waitingTime;

	public int getId() {
		return id;
	}

	public String getIdAsString() {
		return String.valueOf(id);
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCustomersWaiting() {
		return customersWaiting;
	}

	public void setCustomersWaiting(int customersWaiting) {
		this.customersWaiting = customersWaiting;
	}

	public int getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}
	
	@Override
	public String toString() {
		return "DTOQueue [id=" + id + ", Name=" + name + ", customersWaiting=" + customersWaiting + ", waitingTime="
				+ waitingTime + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + customersWaiting;
		result = prime * result + id;
		result = prime * result + waitingTime;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DTOQueue other = (DTOQueue) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (customersWaiting != other.customersWaiting)
			return false;
		if (id != other.id)
			return false;
        return waitingTime == other.waitingTime;
    }
}
