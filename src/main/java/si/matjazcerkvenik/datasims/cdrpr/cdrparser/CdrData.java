package si.matjazcerkvenik.datasims.cdrpr.cdrparser;

import si.matjazcerkvenik.datasims.cdrpr.cdr.parser.CdrBean;
import si.matjazcerkvenik.datasims.cdrpr.cdr.parser.PpdrBean;

import java.util.ArrayList;
import java.util.List;

public class CdrData {

    public String fileName;
    public String nodeName;
    public List<CdrBean> cdrList = new ArrayList<>();
    public List<PpdrBean> ppdrList = new ArrayList<>();

}
