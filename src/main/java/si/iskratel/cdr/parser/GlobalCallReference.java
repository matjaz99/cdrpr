package si.iskratel.cdr.parser;

public class GlobalCallReference {
  private boolean received;

  private long networkId;

  private long nodeId;

  private long callReference;

  public boolean isReceived() {
    return received;
  }

  public void setReceived(boolean received) {
    this.received = received;
  }

  public long getNetworkId() {
    return networkId;
  }

  public void setNetworkId(long networkId) {
    this.networkId = networkId;
  }

  public long getNodeId() {
    return nodeId;
  }

  public void setNodeId(long nodeId) {
    this.nodeId = nodeId;
  }

  public long getCallReference() {
    return callReference;
  }

  public void setCallReference(long callReference) {
    this.callReference = callReference;
  }

  public String getNodeIdString() {
    String nodeIdString;
    if (networkId != 0) {
      nodeIdString = "" + networkId + "" + nodeId;
    } else {
      nodeIdString = "" + nodeId;
    }
    return nodeIdString;
  }
  

  public String toString() {
    return "callReference" + " " + callReference + " " + 
           "received" + " " + received + " " + 
           "networkId" + " " + networkId + " " + 
           "nodeId" + " " + nodeId;
  }  
}
