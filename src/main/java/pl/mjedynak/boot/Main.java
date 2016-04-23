package pl.mjedynak.boot;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import pl.mjedynak.BatchConfiguration;

import java.util.concurrent.ExecutorService;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.System.currentTimeMillis;

public class Main {

    public static final String START_TIME_PARAMETER = "START_TIME";

    public static final String EXCEPTIONS_FILE = "exceptions.txt";
    public static final String PRIME_FACTOR_FILE = "primeFactorCounter.txt";
    public static final String SUMMARY_FILE = "summary.txt";

    private static void start() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BatchConfiguration.class);
        Job job = (Job) context.getBean("processNumbersJob");
        JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
        jobLauncher.run(job, new JobParameters(of(START_TIME_PARAMETER, new JobParameter(currentTimeMillis()))));

        exitGracefully(context);
    }

    private static void exitGracefully(AnnotationConfigApplicationContext context) {
        ConcurrentTaskExecutor taskExecutor = (ConcurrentTaskExecutor) context.getBean("taskExecutor");
        ExecutorService executor = (ExecutorService) taskExecutor.getConcurrentExecutor();
        executor.shutdown();
    }

    public static void main(String[] args) throws Exception {
        Main.start();
    }

}
