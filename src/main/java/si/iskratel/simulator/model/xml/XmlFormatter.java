package si.iskratel.simulator.model.xml;

import si.iskratel.cdrparser.CdrData;
import si.iskratel.metricslib.PMultiValueMetric;
import si.iskratel.metricslib.PMultivalueTimeSeries;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class XmlFormatter {

    public static CdrAggs getCdrAggsRootElement(CdrData data, String filename, PMultiValueMetric mv_metric) {

        CdrAggs cdrAggs = new CdrAggs();

        CdrAggs.Statistics.Node node = new CdrAggs.Statistics.Node();
        CdrAggs.Statistics.CallStats nodeCallStats = new CdrAggs.Statistics.CallStats();
        nodeCallStats.setCounters(getNodeCounters(mv_metric));
        nodeCallStats.setCauses(getNodeCauses(mv_metric));
        nodeCallStats.setKpis(getNodeKpis(mv_metric));
        node.setCallStats(nodeCallStats);

        CdrAggs.Statistics.TrunkGroups trunkGroups = new CdrAggs.Statistics.TrunkGroups();
        List<CdrAggs.Statistics.TrunkGroups.TrunkGroup> trunkGroupList = new ArrayList<>();

        for (PMultivalueTimeSeries ts : mv_metric.getMultivalueMetrics()) {
            if (ts.getLabelsMap().get("metric").equals("tgStats")) {

                CdrAggs.Statistics.TrunkGroups.TrunkGroup tg = new CdrAggs.Statistics.TrunkGroups.TrunkGroup();
                tg.setId(ts.getLabelsMap().getOrDefault("trunkGroup.id", "-"));
                tg.setName(ts.getLabelsMap().getOrDefault("trunkGroup.name", "-"));
                tg.setDirection(ts.getLabelsMap().getOrDefault("trunkGroup.direction", "-"));

                CdrAggs.Statistics.CallStats callStats = new CdrAggs.Statistics.CallStats();
                callStats.setCounters(getTrunkGroupCounters(ts));
                callStats.setCauses(getTrunkGroupCauses(ts));
                callStats.setKpis(getTgKpis(ts));

                tg.setCallStats(callStats);
                trunkGroupList.add(tg);
            }
        }

        trunkGroups.setTrunkGroup(trunkGroupList);

        CdrAggs.Statistics statistics = new CdrAggs.Statistics();
        statistics.setNode(node);
        statistics.setTrunkGroups(trunkGroups);
        cdrAggs.setStatistics(statistics);

        CdrAggs.Metadata metadata = XmlFormatter.getMetadata(data, filename);
        cdrAggs.setMetadata(metadata);

        return cdrAggs;

    }

    private static CdrAggs.Metadata getMetadata(CdrData data, String filename) {
        CdrAggs.Metadata metadata = new CdrAggs.Metadata();

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        try {
            metadata.setStartTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
            metadata.setEndTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        metadata.setHostname(data.nodeName);
        metadata.setNodeId(data.nodeName);
        metadata.setFilename(filename);
        metadata.setProductCategory("IA");

        return metadata;
    }


    private static CdrAggs.Statistics.CallStats.Counters getNodeCounters(PMultiValueMetric mv_metric) {

        for (PMultivalueTimeSeries ts : mv_metric.getMultivalueMetrics()) {

            if (ts.getLabelsMap().get("metric").equals("nodeStats")) {

                CdrAggs.Statistics.CallStats.Counters counters = new CdrAggs.Statistics.CallStats.Counters();
                List<CdrAggs.Statistics.CallStats.Counters.Counter> counterList = new ArrayList<>();

                CdrAggs.Statistics.CallStats.Counters.Counter nodeRecords = new CdrAggs.Statistics.CallStats.Counters.Counter();
                nodeRecords.setName("node.records");
                nodeRecords.setValue(ts.getValuesMap().getOrDefault("node.records", 0.0).longValue());
                counterList.add(nodeRecords);

                CdrAggs.Statistics.CallStats.Counters.Counter nodeSeizures = new CdrAggs.Statistics.CallStats.Counters.Counter();
                nodeSeizures.setName("node.seizures");
                nodeSeizures.setValue(ts.getValuesMap().getOrDefault("node.seizures", 0.0).longValue());
                counterList.add(nodeSeizures);

                CdrAggs.Statistics.CallStats.Counters.Counter nodeSeizWAns = new CdrAggs.Statistics.CallStats.Counters.Counter();
                nodeSeizWAns.setName("node.seizuresWithAnswer");
                nodeSeizWAns.setValue(ts.getValuesMap().getOrDefault("node.seizuresWithAnswer", 0.0).longValue());
                counterList.add(nodeSeizWAns);

                CdrAggs.Statistics.CallStats.Counters.Counter nodeActive = new CdrAggs.Statistics.CallStats.Counters.Counter();
                nodeActive.setName("node.activeCalls");
                nodeActive.setValue(ts.getValuesMap().getOrDefault("node.activeCalls", 0.0).longValue());
                counterList.add(nodeActive);

                CdrAggs.Statistics.CallStats.Counters.Counter nodeDur = new CdrAggs.Statistics.CallStats.Counters.Counter();
                nodeDur.setName("node.duration");
                nodeDur.setUnit("ms");
                nodeDur.setValue(ts.getValuesMap().getOrDefault("node.duration", 0.0).longValue());
                counterList.add(nodeDur);

                counters.setCounter(counterList);

                return counters;
            }
        }

        return null;
    }

    private static CdrAggs.Statistics.CallStats.Causes getNodeCauses(PMultiValueMetric mv_metric) {

        for (PMultivalueTimeSeries ts : mv_metric.getMultivalueMetrics()) {

//            if (ts.getValuesMap().containsKey("cause.answered")) {

                CdrAggs.Statistics.CallStats.Causes causes = new CdrAggs.Statistics.CallStats.Causes();
                List<CdrAggs.Statistics.CallStats.Causes.Cause> causeList = new ArrayList<>();

                CdrAggs.Statistics.CallStats.Causes.Cause causeAns = new CdrAggs.Statistics.CallStats.Causes.Cause();
                causeAns.setId("16");
                causeAns.setName("cause.answered");
                causeAns.setValue(ts.getValuesMap().getOrDefault("cause.answered", 0.0).longValue());
                causeList.add(causeAns);

                CdrAggs.Statistics.CallStats.Causes.Cause causeBusy = new CdrAggs.Statistics.CallStats.Causes.Cause();
                causeBusy.setId("17");
                causeBusy.setName("cause.busy");
                causeBusy.setValue(ts.getValuesMap().getOrDefault("cause.busy", 0.0).longValue());
                causeList.add(causeBusy);

                CdrAggs.Statistics.CallStats.Causes.Cause causeNoResponse = new CdrAggs.Statistics.CallStats.Causes.Cause();
                causeNoResponse.setId("17");
                causeNoResponse.setName("cause.noResponse");
                causeNoResponse.setValue(ts.getValuesMap().getOrDefault("cause.noResponse", 0.0).longValue());
                causeList.add(causeNoResponse);

                CdrAggs.Statistics.CallStats.Causes.Cause causeRejected = new CdrAggs.Statistics.CallStats.Causes.Cause();
                causeRejected.setId("21");
                causeRejected.setName("cause.rejected");
                causeRejected.setValue(ts.getValuesMap().getOrDefault("cause.rejected", 0.0).longValue());
                causeList.add(causeRejected);

                CdrAggs.Statistics.CallStats.Causes.Cause causeOther = new CdrAggs.Statistics.CallStats.Causes.Cause();
                causeOther.setId("0");
                causeOther.setName("cause.other");
                causeOther.setValue(ts.getValuesMap().getOrDefault("cause.other", 0.0).longValue());
                causeList.add(causeOther);

                causes.setCause(causeList);

                return causes;

//            }

        }

        return null;
    }

    private static CdrAggs.Statistics.CallStats.Kpis getNodeKpis(PMultiValueMetric mv_metric) {

        for (PMultivalueTimeSeries ts : mv_metric.getMultivalueMetrics()) {

            if (ts.getValuesMap().containsKey("kpi.trafficIntensity")) {

                CdrAggs.Statistics.CallStats.Kpis kpis = new CdrAggs.Statistics.CallStats.Kpis();
                List<CdrAggs.Statistics.CallStats.Kpis.Kpi> kpiList = new ArrayList<>();

                CdrAggs.Statistics.CallStats.Kpis.Kpi trInt = new CdrAggs.Statistics.CallStats.Kpis.Kpi();
                trInt.setName("kpi.trafficIntensity");
                trInt.setUnit("E");
                trInt.setValue(ts.getValuesMap().getOrDefault("kpi.trafficIntensity", 0.0));
                kpiList.add(trInt);

                CdrAggs.Statistics.CallStats.Kpis.Kpi trVol = new CdrAggs.Statistics.CallStats.Kpis.Kpi();
                trVol.setName("kpi.trafficVolume");
                trVol.setUnit("Eh");
                trVol.setValue(ts.getValuesMap().getOrDefault("kpi.trafficVolume", 0.0));
                kpiList.add(trVol);

                CdrAggs.Statistics.CallStats.Kpis.Kpi asr = new CdrAggs.Statistics.CallStats.Kpis.Kpi();
                asr.setName("kpi.asr");
                asr.setValue(ts.getValuesMap().getOrDefault("kpi.asr", 0.0));
                kpiList.add(asr);

                kpis.setKpi(kpiList);

                return kpis;

            }

        }

        return null;
    }

    private static CdrAggs.Statistics.CallStats.Counters getTrunkGroupCounters(PMultivalueTimeSeries ts) {

        CdrAggs.Statistics.CallStats.Counters tgCounters = new CdrAggs.Statistics.CallStats.Counters();
        List<CdrAggs.Statistics.CallStats.Counters.Counter> tgCounterList = new ArrayList<>();

        CdrAggs.Statistics.CallStats.Counters.Counter tgRecords = new CdrAggs.Statistics.CallStats.Counters.Counter();
        tgRecords.setName("trunkGroup.records");
        tgRecords.setValue(ts.getValuesMap().getOrDefault("trunkGroup.records", 0.0).longValue());
        tgCounterList.add(tgRecords);

        CdrAggs.Statistics.CallStats.Counters.Counter tgSeizures = new CdrAggs.Statistics.CallStats.Counters.Counter();
        tgSeizures.setName("trunkGroup.seizures");
        tgSeizures.setValue(ts.getValuesMap().getOrDefault("trunkGroup.seizures", 0.0).longValue());
        tgCounterList.add(tgSeizures);

        CdrAggs.Statistics.CallStats.Counters.Counter tgSeizWAns = new CdrAggs.Statistics.CallStats.Counters.Counter();
        tgSeizWAns.setName("trunkGroup.seizuresWithAnswer");
        tgSeizWAns.setValue(ts.getValuesMap().getOrDefault("trunkGroup.seizuresWithAnswer", 0.0).longValue());
        tgCounterList.add(tgSeizWAns);

        CdrAggs.Statistics.CallStats.Counters.Counter tgActive = new CdrAggs.Statistics.CallStats.Counters.Counter();
        tgActive.setName("trunkGroup.activeCalls");
        tgActive.setValue(ts.getValuesMap().getOrDefault("trunkGroup.activeCalls", 0.0).longValue());
        tgCounterList.add(tgActive);

        CdrAggs.Statistics.CallStats.Counters.Counter tgDur = new CdrAggs.Statistics.CallStats.Counters.Counter();
        tgDur.setName("trunkGroup.duration");
        tgDur.setUnit("ms");
        tgDur.setValue(ts.getValuesMap().getOrDefault("trunkGroup.duration", 0.0).longValue());
        tgCounterList.add(tgDur);

        tgCounters.setCounter(tgCounterList);

        return tgCounters;
    }

    private static CdrAggs.Statistics.CallStats.Causes getTrunkGroupCauses(PMultivalueTimeSeries ts) {

        CdrAggs.Statistics.CallStats.Causes causes = new CdrAggs.Statistics.CallStats.Causes();
        List<CdrAggs.Statistics.CallStats.Causes.Cause> causeList = new ArrayList<>();

        CdrAggs.Statistics.CallStats.Causes.Cause causeAns = new CdrAggs.Statistics.CallStats.Causes.Cause();
        causeAns.setId("16");
        causeAns.setName("cause.answered");
        causeAns.setValue(ts.getValuesMap().getOrDefault("cause.answered", 0.0).longValue());
        causeList.add(causeAns);

        CdrAggs.Statistics.CallStats.Causes.Cause causeBusy = new CdrAggs.Statistics.CallStats.Causes.Cause();
        causeBusy.setId("17");
        causeBusy.setName("cause.busy");
        causeBusy.setValue(ts.getValuesMap().getOrDefault("cause.busy", 0.0).longValue());
        causeList.add(causeBusy);

        CdrAggs.Statistics.CallStats.Causes.Cause causeNoResponse = new CdrAggs.Statistics.CallStats.Causes.Cause();
        causeNoResponse.setId("17");
        causeNoResponse.setName("cause.noResponse");
        causeNoResponse.setValue(ts.getValuesMap().getOrDefault("cause.noResponse", 0.0).longValue());
        causeList.add(causeNoResponse);

        CdrAggs.Statistics.CallStats.Causes.Cause causeRejected = new CdrAggs.Statistics.CallStats.Causes.Cause();
        causeRejected.setId("21");
        causeRejected.setName("cause.rejected");
        causeRejected.setValue(ts.getValuesMap().getOrDefault("cause.rejected", 0.0).longValue());
        causeList.add(causeRejected);

        CdrAggs.Statistics.CallStats.Causes.Cause causeOther = new CdrAggs.Statistics.CallStats.Causes.Cause();
        causeOther.setId("0");
        causeOther.setName("cause.other");
        causeOther.setValue(ts.getValuesMap().getOrDefault("cause.other", 0.0).longValue());
        causeList.add(causeOther);

        causes.setCause(causeList);

        return causes;
    }

    private static CdrAggs.Statistics.CallStats.Kpis getTgKpis(PMultivalueTimeSeries ts) {

        if (ts.getValuesMap().containsKey("trunkGroup.duration")) {

            CdrAggs.Statistics.CallStats.Kpis kpis = new CdrAggs.Statistics.CallStats.Kpis();
            List<CdrAggs.Statistics.CallStats.Kpis.Kpi> kpiList = new ArrayList<>();

            CdrAggs.Statistics.CallStats.Kpis.Kpi trInt = new CdrAggs.Statistics.CallStats.Kpis.Kpi();
            trInt.setName("kpi.trafficIntensity");
            trInt.setUnit("E");
            trInt.setValue(ts.getValuesMap().getOrDefault("kpi.trafficIntensity", 0.0));
            kpiList.add(trInt);

            CdrAggs.Statistics.CallStats.Kpis.Kpi trVol = new CdrAggs.Statistics.CallStats.Kpis.Kpi();
            trVol.setName("kpi.trafficVolume");
            trVol.setUnit("Eh");
            trVol.setValue(ts.getValuesMap().getOrDefault("kpi.trafficVolume", 0.0));
            kpiList.add(trVol);

            CdrAggs.Statistics.CallStats.Kpis.Kpi asr = new CdrAggs.Statistics.CallStats.Kpis.Kpi();
            asr.setName("kpi.asr");
            asr.setValue(ts.getValuesMap().getOrDefault("kpi.asr", 0.0));
            kpiList.add(asr);

            kpis.setKpi(kpiList);

            return kpis;

        }

        return null;
    }

}
