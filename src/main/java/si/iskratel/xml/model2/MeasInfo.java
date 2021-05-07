package si.iskratel.xml.model2;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;

public class MeasInfo {
	
	private String measInfoId;
	private Job job;
	private GranPeriod granPeriod;
	private RepPeriod repPeriod;
	private String measTypes;
	private MeasValue measValue;
	
	private String[] measurements;
	private int[] values;

	public String getMeasInfoId() {
		return measInfoId;
	}

	@XmlAttribute
	public void setMeasInfoId(String measInfoId) {
		this.measInfoId = measInfoId;
	}

	public Job getJob() {
		return job;
	}

	@XmlElement
	public void setJob(Job job) {
		this.job = job;
	}

	public GranPeriod getGranPeriod() {
		return granPeriod;
	}

	@XmlElement
	public void setGranPeriod(GranPeriod granPeriod) {
		this.granPeriod = granPeriod;
	}

	public RepPeriod getRepPeriod() {
		return repPeriod;
	}

	@XmlElement
	public void setRepPeriod(RepPeriod repPeriod) {
		this.repPeriod = repPeriod;
	}

	public String getMeasTypes() {
		return measTypes;
	}

	@XmlElement
	public void setMeasTypes(String measTypes) {
		this.measTypes = measTypes;
	}

	public MeasValue getMeasValue() {
		return measValue;
	}

	@XmlElement
	public void setMeasValue(MeasValue measValue) {
		this.measValue = measValue;
	}

	@Override
	public String toString() {
		return "MeasInfo{" +
				"measInfoId='" + measInfoId + '\'' +
				", job=" + job +
				", granPeriod=" + granPeriod +
				", repPeriod=" + repPeriod +
				", measTypes='" + measTypes + '\'' +
				", measValue=" + measValue +
				", measurements=" + Arrays.toString(measurements) +
				", values=" + Arrays.toString(values) +
				'}';
	}
}
