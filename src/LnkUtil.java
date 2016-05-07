import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;

//文件快捷方式解析类，解析出快捷方式指向的真实路径，测试用
public class LnkUtil {

    public static void main(String[] args) {
        System.out.println("G:\\sdlkfj/".replaceAll("\\\\", "/"));
    }
    
    public LnkUtil(File f) {
        try {
			parse(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private boolean is_dir;

    public boolean isDirectory() {
        return is_dir;
    }

    private String real_file;

    public String getRealFilename() {
        return real_file;
    }

    public void parse(File f) throws Exception {
        // read the entire file into a byte buffer
        FileInputStream fin = new FileInputStream(f);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buff = new byte[256];
        while (true) {
            int n = fin.read(buff);
            if (n == -1) {
                break;
            }
            bout.write(buff, 0, n);
        }
        fin.close();
        byte[] link = bout.toByteArray();

        // get the flags byte
        byte flags = link[0x14];

        // get the file attributes byte
        final int file_atts_offset = 0x18;
        byte fileatts = link[file_atts_offset];
        byte is_dir_mask = (byte) 0x10;
        if ((fileatts & is_dir_mask) > 0) {
            is_dir = true;
        } else {
            is_dir = false;
        }

        // if the shell settings are present, skip them
        final int shell_offset = 0x4c;
        int shell_len = 0;
        if ((flags & 0x1) > 0) {
            // the plus 2 accounts for the length marker itself
            shell_len = bytes2short(link, shell_offset) + 2;
        }

        // get to the file settings
        int file_start = 0x4c + shell_len;

        // get the local volume and local system values
        int local_sys_off = link[file_start + 0x10] + file_start;
        real_file = getNullDelimitedString(link, local_sys_off);
    }

    static String getNullDelimitedString(byte[] bytes, int off) throws UnsupportedEncodingException {
        int len = 0;
        // count bytes until the null character (0)
        while (true) {
            if (bytes[off + len] == 0) {
                break;
            }
            len++;
        }
        return new String(bytes, off, len, "GBK");
    }

    // convert two bytes into a short
    // note, this is little endian because it's for an
    // Intel only OS.
    static int bytes2short(byte[] bytes, int off) {
        return bytes[off] | (bytes[off + 1] << 8);
    }

    public static String p(String str) {
        return str;
    }
}