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
                "counters",
                "causes"
        })
        public static class CallStats {

            @XmlElement(required = true)
            protected Counters counters;

//            @XmlElement(required = true)
//            protected double trafficIntensity;
//            @XmlElement(required = true)
//            protected double trafficVolume;
//            @XmlElement(required = true)
            @XmlElement(required = true)
            protected Causes causes;

            public Counters getCounters() {
                return counters;
            }

            public void setCounters(Counters counters) {
                this.counters = counters;
            }

            public Causes getCauses() {
                return causes;
            }

            public void setCauses(Causes causes) {
                this.causes = causes;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "counter"
            })
            public static class Counters {

                @XmlElement(required = true, name="counter")
                protected List<Counter> counter;

                public List<Counter> getCounter() {
                    return counter;
                }

                public void setCounter(List<Counter> counter) {
                    this.counter = counter;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Counter {

                    @XmlAttribute(required = true, name="name")
                    protected String name;
                    @XmlAttribute(required = false, name="unit")
                    protected String unit;
                    @XmlValue
                    protected Long value;

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getUnit() {
                        return unit;
                    }

                    public void setUnit(String unit) {
                        this.unit = unit;
                    }

                    public Long getValue() {
                        return value;
                    }

                    public void setValue(Long value) {
                        this.value = value;
                    }
                }

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

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Cause {

                    @XmlAttribute(name = "id", required = true)
                    protected String id;
                    @XmlAttribute(name = "name", required = true)
                    protected String name;
                    @XmlValue
                    protected Long value;

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

                    public Long getValue() {
                        return value;
                    }

                    public void setValue(Long value) {
                        this.value = value;
                    }
                }

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
