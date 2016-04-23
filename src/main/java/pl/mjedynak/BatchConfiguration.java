package pl.mjedynak;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import pl.mjedynak.boot.Main;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final int THREAD_POOL_SIZE = 4;

    @Bean
    public ItemReader<String> reader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("numbers.txt"));
        reader.setLineMapper(new PassThroughLineMapper());
        return reader;
    }

    @Bean
    public ItemProcessor<String, String> processor() {
        return new LineProcessor();
    }

    @Bean
    public ItemWriter<String> writer() {
        FlatFileItemWriter<String> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(Main.PRIME_FACTOR_FILE));
        writer.setLineAggregator(new PassThroughLineAggregator<>());
        return writer;
    }

    @Bean
    public Job processNumbersJob(JobBuilderFactory jobs, Step step1, Step step2) {
        return jobs.get("processNumbersJob")
                .flow(step1)
                .next(step2)
                .end()
                .build();
    }

    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<String> reader,
                      ItemWriter<String> writer, ItemProcessor<String, String> processor) {
        return stepBuilderFactory.get("step1")
                .<String, String>chunk(10)
                .listener(new ErrorListener())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(Integer.MAX_VALUE)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .throttleLimit(THREAD_POOL_SIZE)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step step2(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("step2").tasklet(new WriteSummaryTasklet()).build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new ConcurrentTaskExecutor(newFixedThreadPool(THREAD_POOL_SIZE));
    }

    @Bean
    public JobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository());
        return simpleJobLauncher;
    }

    @Bean
    public JobRepository jobRepository() throws Exception {
        ResourcelessTransactionManager resourcelessTransactionManager = new ResourcelessTransactionManager();
        MapJobRepositoryFactoryBean mapJobRepositoryFactoryBean = new MapJobRepositoryFactoryBean();
        mapJobRepositoryFactoryBean.setTransactionManager(resourcelessTransactionManager);
        return mapJobRepositoryFactoryBean.getObject();
    }

    @Bean
    PrimeFactorCounter primeFactorCounter() {
        return new PrimeFactorCounter();
    }

}
