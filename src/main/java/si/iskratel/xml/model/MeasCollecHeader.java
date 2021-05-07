package si.iskratel.xml.model;

import javax.xml.bind.annotation.XmlAttribute;

public class MeasCollecHeader {
	
	private String beginTime;

	public String getBeginTime() {
		return beginTime;
	}

	@XmlAttribute
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	@Override
	public String toString() {
		return "MeasCollecHeader{" +
				"beginTime='" + beginTime + '\'' +
				'}';
	}


}
