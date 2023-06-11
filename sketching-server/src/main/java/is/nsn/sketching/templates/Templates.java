package is.nsn.sketching.templates;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import is.nsn.sketching.nDollar.MultistrokeR;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

public class Templates {
    private static final String FILE_NAME = "templates.json";
    private static File getTemplateFolderPath() {
        return new File(System.getProperty("user.home") + File.separator + "nsn_sketching");
    }

    public static Hashtable<String, MultistrokeR> getTemplates() throws IOException {
        File templatePath = new File(getTemplateFolderPath(), FILE_NAME);
        if (templatePath.exists()) {
            return new ObjectMapper().readValue(templatePath, new TypeReference<Hashtable<String, MultistrokeR>>() {
            });
        }

        return new Hashtable<>();
    }

    public static void storeTemplate(String key, MultistrokeR strokes) throws IOException {
        Hashtable<String, MultistrokeR> templates = getTemplates();
        templates.put(key, strokes);

        String json = new ObjectMapper().writeValueAsString(templates);

        File templateFolder = getTemplateFolderPath();

        if (!templateFolder.exists()) {
           if(!templateFolder.mkdirs() || !(new File(getTemplateFolderPath(), FILE_NAME).createNewFile())) {
               System.out.println("Failed to write templates file in folder:" + templateFolder.getAbsolutePath());
           }
        }

        FileWriter fw = new FileWriter(new File(getTemplateFolderPath(), FILE_NAME), false);
        fw.write(json);
        fw.close();
    }
}
