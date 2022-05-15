package si.iskratel.simulator.model.itxml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CdrAggs {

    private Metadata metadata;
    private Statistics statistics;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
}
