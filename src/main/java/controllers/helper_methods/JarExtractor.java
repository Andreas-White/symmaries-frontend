package controllers.helper_methods;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarExtractor {

    public void extractClassFiles(String jarFile, String destDir) throws IOException {
        JarFile jar = new java.util.jar.JarFile(jarFile);
        Enumeration<JarEntry> enumEntries = jar.entries();

        while (enumEntries.hasMoreElements()) {
            JarEntry file = enumEntries.nextElement();
            File f = new File(destDir + File.separator + file.getName());
            f.getParentFile().mkdirs();

            if (file.isDirectory()) { // if it's a directory, create it
                continue;
            }

            if (!file.getName().endsWith(".class")) continue;
            InputStream is = jar.getInputStream(file); // get the input stream
            FileOutputStream fos = new FileOutputStream(f);

            while (is.available() > 0) {  // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
        jar.close();
    }
}
