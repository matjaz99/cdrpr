package si.iskratel.xml.model2;

import javax.xml.bind.annotation.XmlAttribute;

public class RepPeriod {
	
	private String duration;

	public String getDuration() {
		return duration;
	}

	@XmlAttribute
	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "RepPeriod{" +
				"duration='" + duration + '\'' +
				'}';
	}
}
