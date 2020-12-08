package si.iskratel.metricslib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checksum {

    /**
     * Return MD5 checksum of a string.
     * @param s
     * @return checksum
     */
    public static String getMd5Checksum(String s) {

        StringBuffer sb = new StringBuffer("");

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] dataBytes = s.getBytes();

            md.update(dataBytes, 0, dataBytes.length);

            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sb.toString();

    }

}
