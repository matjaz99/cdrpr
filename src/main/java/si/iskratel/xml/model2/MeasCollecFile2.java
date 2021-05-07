package si.iskratel.xml.model2;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="measCollecFile")
//@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="", propOrder={"fileHeader", "measData", "fileFooter"})
public class MeasCollecFile2 {
	
	private String xmlns = "http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec";
	private String xmlns_xsi = "http://www.w3.org/2001/XMLSchema-instance";
	private String xsi_schemaLocation = "http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec";
	
	private FileHeader fileHeader;
	private MeasData measData;
	private FileFooter fileFooter;
	
	
	public String getXmlns() {
		return xmlns;
	}

	@XmlAttribute(name="xmlns")
	public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}

	public String getXmlns_xsi() {
		return xmlns_xsi;
	}

	@XmlAttribute(name="xmlns:xsi")
	public void setXmlns_xsi(String xmlns_xsi) {
		this.xmlns_xsi = xmlns_xsi;
	}

	public String getXsi_schemaLocation() {
		return xsi_schemaLocation;
	}

	@XmlAttribute(name="xsi:schemaLocation")
	public void setXsi_schemaLocation(String xsi_schemaLocation) {
		this.xsi_schemaLocation = xsi_schemaLocation;
	}

	public FileHeader getFileHeader() {
		return fileHeader;
	}

	@XmlElement
	public void setFileHeader(FileHeader fileHeader) {
		this.fileHeader = fileHeader;
	}

	public MeasData getMeasData() {
		return measData;
	}

	@XmlElement
	public void setMeasData(MeasData measData) {
		this.measData = measData;
	}

	public FileFooter getFileFooter() {
		return fileFooter;
	}

	@XmlElement
	public void setFileFooter(FileFooter fileFooter) {
		this.fileFooter = fileFooter;
	}

	@Override
	public String toString() {
		return "MeasCollecFile{" +
				"fileHeader=" + fileHeader +
				", measData=" + measData +
				", fileFooter=" + fileFooter +
				'}';
	}
}
