package pl.mjedynak;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.io.File;
import java.io.IOException;

import static java.lang.String.valueOf;
import static org.apache.commons.io.FileUtils.writeStringToFile;

public class DataGenerator {

    public static void main(String[] args) throws IOException {
        RandomDataGenerator generator = new RandomDataGenerator();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            String number;
            if (i % 100 == 0) {
                number = valueOf(generator.nextLong(-9_999_999_999_999L, -1_000_000_000_000L));
            } else {
                number = valueOf(generator.nextLong(1_000_000_000_000L, 9_999_999_999_999L));
            }
            sb.append(number).append("\n");
        }
        writeStringToFile(new File("numbers.txt"), sb.toString());
    }
}

