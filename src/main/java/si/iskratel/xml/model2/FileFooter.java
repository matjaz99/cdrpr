package si.iskratel.xml.model2;

import javax.xml.bind.annotation.XmlElement;

public class FileFooter {
	
	private MeasCollecFooter measCollec;

	public MeasCollecFooter getMeasCollec() {
		return measCollec;
	}

	@XmlElement(name = "measCollec")
	public void setMeasCollec(MeasCollecFooter measCollec) {
		this.measCollec = measCollec;
	}

	@Override
	public String toString() {
		return "FileFooter{" +
				"measCollec=" + measCollec +
				'}';
	}
}
