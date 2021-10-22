package si.iskratel.cdrparser;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.cdr.parser.PpdrBean;

import java.util.ArrayList;
import java.util.List;

public class CdrData {

    public String nodeName;
    public List<CdrBean> cdrList = new ArrayList<>();
    public List<PpdrBean> ppdrList = new ArrayList<>();

}
