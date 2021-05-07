package si.iskratel.xml.model;

import javax.xml.bind.annotation.XmlAttribute;

public class FileSender {

	private String elementType;
	private String localDn;

	public String getElementType() {
		return elementType;
	}

	@XmlAttribute
	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	/**
	 * Get nodeID
	 * @return localDn
	 */
	public String getLocalDn() {
		return localDn;
	}

	/**
	 * Set nodeID
	 * @param localDn
	 */
	@XmlAttribute
	public void setLocalDn(String localDn) {
		this.localDn = localDn;
	}

}
