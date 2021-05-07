package si.iskratel.xml.model2;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class MeasValue {
	
	private String measObjLdn = "Group1";
	private String measResults;

	public String getMeasObjLdn() {
		return measObjLdn;
	}

	@XmlAttribute
	public void setMeasObjLdn(String measObjLdn) {
		this.measObjLdn = measObjLdn;
	}

	public String getMeasResults() {
		return measResults;
	}

	@XmlElement
	public void setMeasResults(String measResults) {
		this.measResults = measResults;
	}

	@Override
	public String toString() {
		return "MeasValue{" +
				"measObjLdn='" + measObjLdn + '\'' +
				", measResults='" + measResults + '\'' +
				'}';
	}
}
