package pl.mjedynak;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.ItemProcessListener;
import pl.mjedynak.boot.Main;

import java.io.File;
import java.io.IOException;

public class ErrorListener implements ItemProcessListener<String, String> {

    @Override
    public void beforeProcess(String item) {
    }

    @Override
    public void afterProcess(String item, String result) {
    }

    @Override
    public void onProcessError(String item, Exception e) {
        try {
            FileUtils.write(new File(Main.EXCEPTIONS_FILE), item + ", " + e + "\n", true);
        } catch (IOException e1) {
            throw new RuntimeException(e);
        }
    }
}
