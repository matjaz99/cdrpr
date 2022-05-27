package si.iskratel.simulator.model.xml;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "metadata",
        "statistics"
})
@XmlRootElement(name = "cdrAggs")
public class CdrAggs {

    @XmlElement(required = true)
    protected Metadata metadata;
    @XmlElement(required = true)
    protected Statistics statistics;

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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "startTime",
            "endTime",
            "nodeId",
            "productCategory",
            "hostname",
            "filename"
    })
    public static class Metadata {

        @XmlElement(name = "startTime", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar startTime;
        @XmlElement(name = "endTime", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar endTime;
        @XmlElement(required = true)
        protected String nodeId;
        @XmlElement(required = true)
        protected String productCategory;
        @XmlElement(required = true)
        protected String hostname;
        @XmlElement(required = true)
        protected String filename;

        public XMLGregorianCalendar getStartTime() {
            return startTime;
        }

        public void setStartTime(XMLGregorianCalendar startTime) {
            this.startTime = startTime;
        }

        public XMLGregorianCalendar getEndTime() {
            return endTime;
        }

        public void setEndTime(XMLGregorianCalendar endTime) {
            this.endTime = endTime;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getProductCategory() {
            return productCategory;
        }

        public void setProductCategory(String productCategory) {
            this.productCategory = productCategory;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "node",
            "trunkGroups"
    })
    public static class Statistics {

        @XmlElement(required = true)
        protected Node node;
        @XmlElement(required = true)
        protected TrunkGroups trunkGroups;

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }

        public TrunkGroups getTrunkGroups() {
            return trunkGroups;
        }

        public void setTrunkGroups(TrunkGroups trunkGroups) {
            this.trunkGroups = trunkGroups;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "callStats"
        })
        public static class Node {

            @XmlElement(required = true)
            protected CallStats callStats;

            public CallStats getCallStats() {
                return callStats;
            }

            public void setCallStats(CallStats callStats) {
                this.callStats = callStats;
            }
        }

        // TODO trunk groups

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "records",
                "seizures",
                "seizuresWithAnswer",
                "active",
                "duration",
                "trafficIntensity",
                "trafficVolume",
                "timeBeforeRinging",
                "timeBeforeAnswer",
                "causes"
        })
        public static class CallStats {

            @XmlElement(required = true)
            protected long records;
            @XmlElement(required = true)
            protected Integer seizures;
            @XmlElement(required = true)
            protected Integer seizuresWithAnswer;
            @XmlElement(required = true)
            protected Double active;
            @XmlElement(required = true)
            protected Double duration;
            @XmlElement(required = true)
            protected double trafficIntensity;
            @XmlElement(required = true)
            protected double trafficVolume;
            @XmlElement(required = true)
            protected long timeBeforeRinging;
            @XmlElement(required = true)
            protected long timeBeforeAnswer;
            @XmlElement(required = true)
            protected Causes causes;

            public long getRecords() {
                return records;
            }

            public void setRecords(long records) {
                this.records = records;
            }

            public Integer getSeizures() {
                return seizures;
            }

            public void setSeizures(Integer seizures) {
                this.seizures = seizures;
            }

            public Integer getSeizuresWithAnswer() {
                return seizuresWithAnswer;
            }

            public void setSeizuresWithAnswer(Integer seizuresWithAnswer) {
                this.seizuresWithAnswer = seizuresWithAnswer;
            }

            public Double getActive() {
                return active;
            }

            public void setActive(Double active) {
                this.active = active;
            }

            public Double getDuration() {
                return duration;
            }

            public void setDuration(Double duration) {
                this.duration = duration;
            }

            public double getTrafficIntensity() {
                return trafficIntensity;
            }

            public void setTrafficIntensity(double trafficIntensity) {
                this.trafficIntensity = trafficIntensity;
            }

            public double getTrafficVolume() {
                return trafficVolume;
            }

            public void setTrafficVolume(double trafficVolume) {
                this.trafficVolume = trafficVolume;
            }

            public long getTimeBeforeRinging() {
                return timeBeforeRinging;
            }

            public void setTimeBeforeRinging(long timeBeforeRinging) {
                this.timeBeforeRinging = timeBeforeRinging;
            }

            public long getTimeBeforeAnswer() {
                return timeBeforeAnswer;
            }

            public void setTimeBeforeAnswer(long timeBeforeAnswer) {
                this.timeBeforeAnswer = timeBeforeAnswer;
            }

            public Causes getCauses() {
                return causes;
            }

            public void setCauses(Causes causes) {
                this.causes = causes;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "cause"
            })
            public static class Causes {

                @XmlElement(required = true, name="cause")
                protected List<Cause> cause;

                public List<Cause> getCause() {
                    return cause;
                }

                public void setCause(List<Cause> cause) {
                    this.cause = cause;
                }

//                @XmlAccessorType(XmlAccessType.FIELD)
//                @XmlType(name = "")
//                public static class Cause {
//
//                    @XmlAttribute(name = "id", required = true)
//                    protected String id;
//                    @XmlAttribute(name = "name", required = true)
//                    protected String name;
//                    @XmlElement(required = true)
//                    protected long value;
//
//                    public String getId() {
//                        return id;
//                    }
//
//                    public void setId(String id) {
//                        this.id = id;
//                    }
//
//                    public String getName() {
//                        return name;
//                    }
//
//                    public void setName(String name) {
//                        this.name = name;
//                    }
//
//                    public long getValue() {
//                        return value;
//                    }
//
//                    public void setValue(long value) {
//                        this.value = value;
//                    }
//                }

            }

        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "trunkGroup"
        })
        public static class TrunkGroups {

            @XmlElement(required = true, name="trunkGroup")
            protected List<TrunkGroup> trunkGroup;

            public List<TrunkGroup> getTrunkGroup() {
                return trunkGroup;
            }

            public void setTrunkGroup(List<TrunkGroup> trunkGroup) {
                this.trunkGroup = trunkGroup;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "callStats"
            })
            public static class TrunkGroup {

                @XmlAttribute(name = "id", required = true)
                protected String id;
                @XmlAttribute(name = "name", required = true)
                protected String name;
                @XmlAttribute(name = "direction", required = true)
                protected String direction;
                @XmlElement(required = true)
                protected CallStats callStats;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getDirection() {
                    return direction;
                }

                public void setDirection(String direction) {
                    this.direction = direction;
                }

                public CallStats getCallStats() {
                    return callStats;
                }

                public void setCallStats(CallStats callStats) {
                    this.callStats = callStats;
                }

            }
        }

    }


}
