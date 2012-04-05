package com.cyanogenmod.settings.device;

import java.io.File;

public class Utils {

    /**
     * Check if the specified file exists.
     * @param filename      The filename
     * @return              Whether the file exists or not
     */
    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }

}
