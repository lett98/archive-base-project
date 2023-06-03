package delete.job;

import common.model.trk.EntityTrk;
import common.repository.tracing.ModelTracingRepository;
import common.repository.trk.EntityTrkRepository;
import common.service.CMService;
import delete.job.listener.DeleteChunkListener;
import delete.job.listener.DeleteChunkNightlyListener;
import delete.job.listener.DeleteItemWriteListener;
import delete.job.listener.DeletePartitionerAndJobListener;
import delete.job.step.DeleteUpdater;
import delete.service.DeleteService;
import delete.service.TrackingService;
import lombok.Setter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.List;

import static delete.util.TimeUtil.isHotTime;

@EnableBatchProcessing
@Configuration
public class DeleteJob {
    @Setter(onMethod = @__(@Autowired))
    protected JobBuilderFactory jobs;
    @Setter(onMethod = @__(@Autowired))
    protected StepBuilderFactory steps;
    @Setter(onMethod = @__(@Autowired))
    private EntityTrkRepository entityTrkRepository;
    @Setter(onMethod = @__(@Autowired))
    private ModelTracingRepository modelTracingRepository;
    @Setter(onMethod = @__(@Autowired))
    private TrackingService trackingService;
    @Setter(onMethod = @__(@Autowired))
    private DeleteService deleteService;

    @Setter(onMethod = @__(@Autowired))
    private CMService cmService;

    @Value("${delete.partition.number}")
    private Integer NUMBER_OF_PARTITIONER;

    //TODO
    private String masterStepName;
    private String slaveStepName;
    private String jobName;

    @Bean
    public DeletePartitionerAndJobListener deletingPartitionerAndJobListener() {
        DeletePartitionerAndJobListener partitioner = new DeletePartitionerAndJobListener();
        partitioner.setEntityTrkRepository(entityTrkRepository);
        partitioner.setModelTracingRepository(modelTracingRepository);
        partitioner.setCmService(cmService);
        return partitioner;
    }

    @Bean
    @StepScope
    public ListItemReader<EntityTrk> deletingReader(@Value("#{stepExecutionContext['entityTrk']}") Object entityTrkList) {
        List<EntityTrk> entityTrks = (List<EntityTrk>) entityTrkList;
        return new ListItemReader<>(entityTrks);
    }

    @Bean
    @StepScope
    public DeleteUpdater deletingWriter(@Value("#{stepExecution.jobExecution.jobId}") Object jobId) {
        Long deleteJobId = (Long) jobId;
        DeleteUpdater deleteUpdater =  new DeleteUpdater();
        deleteUpdater.setDeleteJobId(deleteJobId);
        deleteUpdater.setDeleteService(deleteService);
        deleteUpdater.setTrackingService(trackingService);
        return deleteUpdater;
    }
    @Bean
    public DeleteChunkListener chunkListener() {return new DeleteChunkListener();}

    @Bean
    public DeleteChunkNightlyListener chunkNightlyListener() {return new DeleteChunkNightlyListener();}

    @Bean
    public DeleteItemWriteListener writeListener() {return new DeleteItemWriteListener();}
    @Bean
    @JobScope
    public Step deletingMasterStep(@Value("#{jobParameters['chunkSize']}") Object cs) {
        Integer chunkSize = ((Long) cs).intValue();
        return steps.get(masterStepName)
                .partitioner(deletingSlaveStep(chunkSize).getName(), deletingPartitionerAndJobListener())
                .step(deletingSlaveStep(chunkSize))
                .gridSize(NUMBER_OF_PARTITIONER)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .allowStartIfComplete(true)
                .build();
    }

    public Step deletingSlaveStep(Integer chunkSize) {
        SimpleStepBuilder builder = steps.get(slaveStepName)
                .<EntityTrk, EntityTrk>chunk(chunkSize)
                .reader(deletingReader(null))
                .writer(deletingWriter(null))
                .listener(writeListener());

        if(isHotTime()) {
            builder.listener(chunkListener());
        } else {
            builder.listener(chunkNightlyListener());
        }
        return builder.build();
    }

    @Bean
    public Job job() {
        return jobs.get(jobName)
                .listener(deletingPartitionerAndJobListener())
                .start(deletingMasterStep(null))
                .build();
    }
}
