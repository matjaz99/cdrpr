package si.iskratel.xml.model2;

import javax.xml.bind.annotation.XmlAttribute;

public class MeasCollecFooter {
	
	private String endTime;

	public String getEndTime() {
		return endTime;
	}

	@XmlAttribute
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "MeasCollecFooter{" +
				"endTime='" + endTime + '\'' +
				'}';
	}
}
