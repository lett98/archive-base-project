package insert.job;

import common.model.trk.EntityTrk;
import common.repository.trk.EntityTrkRepository;
import common.service.CMService;
import insert.job.listener.InsertItemWriteListener;
import insert.job.listener.InsertPartitionerAndJobListener;
import insert.job.step.InsertProcessor;
import insert.job.step.InsertWriter;
import insert.service.InsertArcService;
import insert.service.InsertService;
import insert.service.TrackingService;
import lombok.Setter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.util.List;

@Configuration
@EnableBatchProcessing
public class InsertJob {
    @Setter(onMethod = @__(@Autowired))
    protected JobBuilderFactory jobs;
    @Setter(onMethod = @__(@Autowired))
    protected StepBuilderFactory steps;
    @Autowired
    private InsertService insertService;
    @Autowired
    private TrackingService trackingService;

    @Autowired
    private InsertArcService insertArcService;
    @Autowired
    private CMService cmService;
    @Autowired
    private EntityTrkRepository entityTrkRepository;

    @Value("${insert.partition.number}")
    private Integer NUMBER_OF_PARTITIONER;
    @Value("${insert.batchsize}")
    private Integer BATCH_SIZE;

    private String jobName;
    private String masterStepName;
    private String slaveStepName;

    @Bean
    public InsertPartitionerAndJobListener insertPartitionerAndJobListener() {
        InsertPartitionerAndJobListener partitioner = new InsertPartitionerAndJobListener();
        partitioner.setEntityTrkRepository(entityTrkRepository);
        partitioner.setCmService(cmService);
        return partitioner;
    }

    @StepScope
    @Bean
    public ListItemReader<EntityTrk> insertReader(@Value("#{stepExecutionContext['entityTrk']}") Object entityTrks) {
        List<EntityTrk> entityTrkList = (List<EntityTrk>) entityTrks;
        return new ListItemReader<>(entityTrkList);
    }

    @StepScope
    @Bean
    public ItemProcessor<EntityTrk, EntityTrk> insertProcessor() {
        return new InsertProcessor();
    }

    @Bean
    public InsertItemWriteListener writeListener() {return new InsertItemWriteListener();}

    @StepScope
    @Bean
    public InsertWriter insertWriter(@Value("#{stepExecution.jobExecution.jobId}") Object jobId) {
        Long insertJobId = (Long) jobId;
        InsertWriter insertWriter = new InsertWriter();
        insertWriter.setInsertedJobId(insertJobId);
        insertWriter.setInsertService(insertService);
        insertWriter.setJobName(jobName);
        insertWriter.setInsertArcService(insertArcService);
        insertWriter.setTrackingService(trackingService);
        return insertWriter;
    }

    @Bean
    public Step insertSlaveStep() {
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(Propagation.REQUIRED.value());
        attribute.setIsolationLevel(Isolation.REPEATABLE_READ.value());
        attribute.setTimeout(30);

        return steps.get(slaveStepName)
                .<EntityTrk, EntityTrk>chunk(BATCH_SIZE)
                .reader(insertReader(null))
                .processor(insertProcessor())
                .writer(insertWriter(null))
                .listener(writeListener())
                .build();
    }

    @Bean
    public Step insertMasterStep() {
        return steps.get(masterStepName)
                .partitioner(insertSlaveStep().getName(), insertPartitionerAndJobListener())
                .step(insertSlaveStep())
                .gridSize(NUMBER_OF_PARTITIONER)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Job job() {
        return jobs.get(jobName)
                .listener(insertPartitionerAndJobListener())
                .start(insertMasterStep())
                .build();
    }
}
