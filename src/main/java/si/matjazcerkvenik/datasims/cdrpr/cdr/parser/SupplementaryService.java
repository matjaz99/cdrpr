package si.matjazcerkvenik.datasims.cdrpr.cdr.parser;

public class SupplementaryService {
	
	private short serviceId;
	private char[] serviceInfo;
	private int inputType;
	
	public SupplementaryService(){}
	
	public SupplementaryService(short serviceId, char[] serviceInfo){
	  this.serviceId = serviceId;
	  this.serviceInfo = serviceInfo;
	}

	public short getServiceId() {
		return serviceId;
	}

	public void setServiceId(short aserviceId) {
		this.serviceId = aserviceId;
	}

	public char[] getServiceInfo() {
		return serviceInfo;
	}

	public void setServiceInfo(char[] aserviceInfo) {
		this.serviceInfo = aserviceInfo;
	}

  public int getInputType() {
    return inputType;
  }

  public void setInputType(int inputType) {
    this.inputType = inputType;
  }	
}

