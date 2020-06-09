package si.iskratel.cdr.parser;

public class PartyNumber {

  private int natureOfAddress = NatureOfAddress.UNKNOWN.getCode();

  private int numberingPlan = NumberingPlan.UNKNOWN.getCode();

  private int presentationInd = PresentationInd.ALLOWED.getCode();

  private int screeningInd = ScreeningInd.USER_PROVIDED_NOT_SCREENED.getCode();
  
  private int lacLength;

  private String number;

  public void setNumberingPlan(int anumberingPlan) {
    this.numberingPlan = anumberingPlan;
  }

  public int getNumberingPlan() {
    return numberingPlan;
  }

  public void setNatureOfAddress(int anatureOfAddress) {
    this.natureOfAddress = anatureOfAddress;
  }

  public int getNatureOfAddress() {
    return natureOfAddress;
  }

  public void setLacLength(int alacLength) {
    this.lacLength = alacLength;
  }

  public int getLacLength() {
    return lacLength;
  }

  public void setNumber(String anumber) {
    this.number = anumber;
  }

  public String getNumber() {
    return number;
  }

  public int getPresentationInd() {
    return presentationInd;
  }

  public void setPresentationInd(int presentationInd) {
    this.presentationInd = presentationInd;
  }

  public int getScreeningInd() {
    return screeningInd;
  }

  public void setScreeningInd(int screeningInd) {
    this.screeningInd = screeningInd;
  }
 

  public String toString() {
    return "number" + " " + number + " " + 
        "lacLength" + " " + lacLength + " " + 
        "natureOfAddress" + " " + natureOfAddress + " " + 
        "presentationInd" + " " + presentationInd + " " + 
        "numberingPlan" + " " + numberingPlan + " " + 
        "screeningInd" + " " + screeningInd;
    
  }  
}

enum NatureOfAddress {

  SPARE(0), SUBSCRIBER_NUMBER(1), UNKNOWN(2), NATIONAL_NUMBER(3), INTERNATIONAL_NUMBER(
      4), NETWORK_SPECIFIC(5), NRN_NATIONAL_SPEC(6), NRN_NETWORK_SPEC(7), NRN_W_CDN(
      8), INTERCITY_OPERATOR(115), INTERNATIONAL_OPERATOR(116);

  private int addressCode;

  NatureOfAddress(int code) {
    this.addressCode = code;
  }

  public int getCode() {
    return addressCode;
  }

  public void setCode(int acode) {
    addressCode = acode;
  }

}

enum NumberingPlan {

  UNKNOWN(0), ISDN(1), DATA(3), TELEX(4), RESERVED_NATIONAL(5), RESERVED_NATIONAL2(
      6), NATIONAL_STANDARD_DSS1(8), PRIVATE_NUMBERING(9);

  private int code;

  NumberingPlan(int acode) {
    this.code = acode;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int acode) {
    code = acode;
  }

}
  enum PresentationInd {

    ALLOWED(0), RESTRICTED(1), NOT_AVAILABLE(2), NOT_INCLUDED(3);

    private int code;

    PresentationInd(int acode) {
      this.code = acode;
    }

    public int getCode() {
      return code;
    }

    public void setCode(int acode) {
      code = acode;
    }
}
  
  enum ScreeningInd {

    USER_PROVIDED_NOT_SCREENED(0), USER_PROVIDED_VERIFIED_PASSED(1), USER_PROVIDED_VERIFIED_FAILED(2), NETWORK_PROVIDED(3);

    private int code;

    ScreeningInd(int acode) {
      this.code = acode;
    }

    public int getCode() {
      return code;
    }

    public void setCode(int acode) {
      code = acode;
    }
}  
