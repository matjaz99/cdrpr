package si.iskratel.xml.model;

import javax.xml.bind.annotation.XmlAttribute;

public class Job {
	
	private String jobId;

	public String getJobId() {
		return jobId;
	}

	@XmlAttribute
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Override
	public String toString() {
		return "Job{" +
				"jobId='" + jobId + '\'' +
				'}';
	}
}
