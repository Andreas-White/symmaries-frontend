package controllers.helper_methods;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.PlainTextOutput;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

public class ClassFileDecompiler {

    public void decompile(String javaFile, String classFile) throws IOException {
        StringWriter out = new StringWriter();
        FileWriter fileWriter = new FileWriter(javaFile);

        try {
            Decompiler.decompile(classFile, new PlainTextOutput(out));

        } finally {
            String result = out.toString();

            fileWriter.write(result);
            fileWriter.close();
        }
    }
}
