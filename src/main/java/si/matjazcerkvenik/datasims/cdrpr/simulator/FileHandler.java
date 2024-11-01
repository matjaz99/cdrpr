package si.matjazcerkvenik.datasims.cdrpr.simulator;

import java.io.File;
import java.util.LinkedList;

public class FileHandler {

    private String cdr_input_dir = "cdr_input_dir";
    private String cdr_output_dir = "cdr_output_dir";

    private File[] nodeDirectories;
    private File[] files;
    private LinkedList<File> filesList = new LinkedList<>();

    public FileHandler(String cdr_input_dir, String cdr_output_dir) {
        this.cdr_input_dir = cdr_input_dir;
        this.cdr_output_dir = cdr_output_dir;
    }

    public File getNextFile() {
        filesList.isEmpty();
        return null;
    }
}
