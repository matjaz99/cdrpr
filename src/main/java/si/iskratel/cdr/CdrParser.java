package si.iskratel.cdr;

import org.apache.commons.io.IOUtils;
import si.iskratel.cdr.manager.BadCdrRecordException;
import si.iskratel.cdr.parser.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class CdrParser {

    public static List<CdrBean> parse(File f) throws Exception {

        List<CdrBean> returnList = new ArrayList<>();

        FileInputStream is = new FileInputStream(f);
//        ByteArrayInputStream bais = new ByteArrayInputStream(is.readAllBytes()); // requires Java 9!!!
        byte[] bytes = IOUtils.toByteArray(is);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        List<DataRecord> list = CDRReader.readDataRecords(bais);
        Start.debug("records in file: " + list.size());

        for (DataRecord dr : list) {
            Start.debug(dr.toString());
            CdrBeanCreator cbc = new CdrBeanCreator() {
                @Override
                public void setSpecificBeanValues(CdrObject cdrObj, CdrBean cdrBean) {

                }
            };
            try {
                CdrBean cdrBean = cbc.parseBinaryCdr(dr.getDataRecordBytes(), null);
                returnList.add(cdrBean);
                Start.totalCount++;
                Start.debug(cdrBean.toString());
            } catch (BadCdrRecordException e) {
                Start.badCdrRecordExceptionCount++;
                PpdrBean ppdrBean = cbc.parseBinaryPpdr(dr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return returnList;

    }

}
