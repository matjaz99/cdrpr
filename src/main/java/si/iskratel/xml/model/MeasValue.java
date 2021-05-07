package si.iskratel.xml.model;

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
	
	
}
