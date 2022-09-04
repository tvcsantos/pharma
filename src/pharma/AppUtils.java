/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pharma;

import java.awt.SystemTray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;

/**
 *
 * @author tvcsantos
 */
public class AppUtils {
    private AppUtils() {};

    public static final String USER_HOME = System.getProperty("user.home");

    public static final String USER_OS = System.getProperty("os.name");

    public static final String USER_OS_VERSION =
            System.getProperty("os.version");

    //public static final String USER_OS_ARCH = getOSArch();

    public static final String USER_JAVA_ARCH = System.getProperty("os.arch");

    public static final boolean OS_SUPPORT_TRAY = SystemTray.isSupported();

    public static final String JAVA_VERSION =
            System.getProperty("java.version");

    /*private static final String getOSArch() {
        if (USER_OS.toLowerCase().contains("windows")) {
            if (System.getenv("ProgramW6432") == null) return "x86";
            else return "x64";
        } else {
            return null;
        }
    }*/

    public static final String FILE_SEPARATOR = java.io.File.separator;

    public static final Runtime RUNTIME = Runtime.getRuntime();

    public static final javax.swing.Icon DEFAULT_HOME_FOLDER_ICON =
            javax.swing.UIManager.getIcon("FileChooser.homeFolderIcon");

    public static final javax.swing.Icon DEFAULT_UP_FOLDER_ICON =
            javax.swing.UIManager.getIcon("FileChooser.upFolderIcon");

    public static final javax.swing.Icon DEFAULT_NEW_FOLDER_ICON =
            javax.swing.UIManager.getIcon("FileChooser.newFolderIcon");

    public static final javax.swing.Icon DEFAULT_LIST_VIEW_ICON =
            javax.swing.UIManager.getIcon("FileChooser.listViewIcon");

    public static final javax.swing.Icon DEFAULT_DETAILS_VIEW_ICON =
            javax.swing.UIManager.getIcon("FileChooser.detailsViewIcon");

    public static final javax.swing.filechooser.FileNameExtensionFilter
            getExtensionFilter(String description, String ... extensions) {
        return new javax.swing.filechooser.FileNameExtensionFilter(
                            description, extensions);
    }

    public static final void useWindows7FileChooserIcons(
            javax.swing.Icon homeFolderIcon, javax.swing.Icon upFolderIcon,
            javax.swing.Icon newFolderIcon, javax.swing.Icon listViewIcon,
            javax.swing.Icon detailsViewIcon) {
        //if (fileChooser == null) throw new java.lang.NullPointerException();
        javax.swing.UIManager.put("FileChooser.homeFolderIcon", homeFolderIcon);
        javax.swing.UIManager.put("FileChooser.upFolderIcon", upFolderIcon);
        javax.swing.UIManager.put("FileChooser.newFolderIcon", newFolderIcon);
        javax.swing.UIManager.put("FileChooser.listViewIcon", listViewIcon);
        javax.swing.UIManager.put(
                "FileChooser.detailsViewIcon", detailsViewIcon);
        //javax.swing.SwingUtilities.updateComponentTreeUI(fileChooser);
    }

    public static final void useDefaultFileChooserIcons() {
        //if (fileChooser == null) throw new java.lang.NullPointerException();
        javax.swing.UIManager.put("FileChooser.homeFolderIcon",
                DEFAULT_HOME_FOLDER_ICON);
        javax.swing.UIManager.put("FileChooser.upFolderIcon",
                DEFAULT_UP_FOLDER_ICON);
        javax.swing.UIManager.put("FileChooser.newFolderIcon",
                DEFAULT_NEW_FOLDER_ICON);
        javax.swing.UIManager.put("FileChooser.listViewIcon",
                DEFAULT_LIST_VIEW_ICON);
        javax.swing.UIManager.put("FileChooser.detailsViewIcon",
                DEFAULT_DETAILS_VIEW_ICON);
        //javax.swing.SwingUtilities.updateComponentTreeUI(fileChooser);
    }

    public static final java.net.URL getLocation(Class<?> theClass) {
        if (theClass == null) throw new java.lang.NullPointerException();
        java.security.ProtectionDomain pd = theClass.getProtectionDomain();
        if (pd == null) return null;
        java.security.CodeSource cs = pd.getCodeSource();
        if (cs == null) return null;
        return cs.getLocation();
    }

    /** Fast & simple file copy. */
    public static void copy(File source, File dest) throws IOException {
        FileChannel in = null, out = null;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(dest).getChannel();

            long size = in.size();
            MappedByteBuffer buf = in.map(
                    FileChannel.MapMode.READ_ONLY, 0, size);

            out.write(buf);

        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private static final boolean osIsWindows =
            USER_OS.toLowerCase().contains("windows");

    private static final boolean osIsLinux =
            USER_OS.toLowerCase().contains("linux");

    private static final boolean osIsSolaris =
            USER_OS.toLowerCase().contains("solaris");

    private static final boolean osIsMac =
            USER_OS.toLowerCase().contains("mac");

    public static final boolean osIsWindows() {
        return osIsWindows;
    }

    public static final boolean osIsLinux() {
        return osIsLinux;
    }

    public static final boolean osIsSolaris() {
        return osIsSolaris;
    }

    public static final boolean osIsMac() {
        return osIsMac;
    }

    public static boolean osIsMacLeopardOrBetter() {
        int minMainVersion = 10;
        int minSubVersion = 5;
        boolean result = false;

        if (osIsMac()) {
            String version = USER_OS_VERSION.toLowerCase();
            StringTokenizer tokenizer = new StringTokenizer(version, ".");
            // check first and second token
            if (tokenizer.countTokens() >= 2) {
                int mainVersion = Integer.parseInt(tokenizer.nextToken());
                int subVersion = Integer.parseInt(tokenizer.nextToken());
                if (mainVersion >= minMainVersion &&
                        subVersion >= minSubVersion) {
                    result = true;
                }
            }
        }

        return result;
    }

}
