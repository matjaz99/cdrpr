package si.iskratel.xml;

public class XmlParser {

    public static void main(String[] args) throws Exception {

        FileLoader fl = new FileLoader();
        Thread t = new Thread(fl);
        t.start();

    }
}
