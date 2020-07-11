package si.iskratel.metricslib;

public class PMultiValueMetric {

    /*
    The idea is to have two types of metrics:
    1. single value metric - multiple labels (string) and only one value (double)
       eg. total_calls{node=node1,cause=answered} 1234
    2. multi value metric - only one label (string) and multiple values (double)
       In PG each row represents value for column name
       eg. total_calls{node=node1} answered=523, busy=134, noreply=32
     */

}
