package seers.irutils;

import java.util.Date;

public class CsvIssue {
	public String key;
	public String issueType;
	public String fixVersions;
	public String resolution;
	public Date resolutionDate;
	public Date created;
	public String priority;
	public Date updated;
	public String status;
	public String components;
	public String description;
	public String summary;
	public String creator;
	public String reporter;
	public String id;
	public String descriptionParsed;
	public String summaryParsed;

	public CsvIssue(String key) {
		this.key = key;
	}

	public CsvIssue() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		CsvIssue other = (CsvIssue) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
