package si.iskratel.xml.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class MeasData {
	
	private ManagedElement managedElement;
	private List<MeasInfo> measInfoList;

	public ManagedElement getManagedElement() {
		return managedElement;
	}

	@XmlElement
	public void setManagedElement(ManagedElement managedElement) {
		this.managedElement = managedElement;
	}

	public List<MeasInfo> getMeasInfoList() {
		return measInfoList;
	}

	@XmlElement(name="measInfo")
	public void setMeasInfoList(List<MeasInfo> measInfoList) {
		this.measInfoList = measInfoList;
	}

	@Override
	public String toString() {
		return "MeasData{" +
				"managedElement=" + managedElement +
				", measInfoList=" + measInfoList +
				'}';
	}
}
