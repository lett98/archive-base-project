package cm.job;

import cm.job.listener.CMPartitionerAndJobListener;
import cm.job.step.CMProcessor;
import cm.job.step.CMWriter;
import cm.model.DataInput;
import cm.model.DataInputMapper;
import common.model.trk.EntityTrk;
import common.repository.trk.CmTrkRepository;
import common.repository.trk.EntityTrkRepository;
import lombok.Setter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.support.ListPreparedStatementSetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableBatchProcessing
@EnableTransactionManagement
public class CMJob {
    @Setter(onMethod = @__(@Autowired))
    private JobBuilderFactory jobs;
    @Setter(onMethod = @__(@Autowired))
    private StepBuilderFactory steps;

    @Setter(onMethod = @__(@Autowired))
    private CmTrkRepository cmTrkRepository;

    @Setter(onMethod = @__(@Autowired))
    private EntityTrkRepository entityTrkRepository;

    @Setter(onMethod = @__({@Autowired, @Qualifier("reposDataSource")}))
    private DataSource jobDataSource;

    @Setter(onMethod = @__({@Autowired, @Qualifier("slaveDataSource")}))
    private DataSource slaveSource;

    @Value("${cm.batchsize}")
    private Integer BATCH_SIZE;

    @Value("${cm.partition.number}")
    private int NUMBER_OF_WINDOWS;

    // For audit bills
    private static final String SELECT_SQL;

    //TODO
    private String masterStepName;
    private String slaveStepName;
    private String jobName;

    @Bean
    public CMPartitionerAndJobListener cmPartitionerAndJobListener() {
        CMPartitionerAndJobListener partitioner = new CMPartitionerAndJobListener();
        partitioner.setCmTrkRepo(cmTrkRepository);
        partitioner.setJobDataSource(jobDataSource);
        partitioner.setMasterStepName(masterStepName);
        return partitioner;
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<DataInput> cmReader(@Value("#{stepExecutionContext['startTime']}") Object startTime,
                                                    @Value("#{stepExecutionContext['endTime']}") Object endTime) {
        JdbcCursorItemReader<DataInput> cursorItemReader = new JdbcCursorItemReader<>();
        Map<String, Object> namedParameters = new HashMap<>() {{
            put("startTime", (Timestamp) startTime);
            put("endTime", (Timestamp) endTime);
        }};
        cursorItemReader.setSql(NamedParameterUtils.substituteNamedParameters(SELECT_SQL, new MapSqlParameterSource(namedParameters)));
        cursorItemReader.setPreparedStatementSetter(
                new ListPreparedStatementSetter(Arrays.asList(NamedParameterUtils.buildValueArray(SELECT_SQL, namedParameters)))
        );
        cursorItemReader.setDataSource(slaveSource);
        cursorItemReader.setRowMapper(new DataInputMapper());

        return cursorItemReader;
    }

    @Bean
    @StepScope
    public ItemProcessor<DataInput, EntityTrk> cmItemProcessor(@Value("#{stepExecution.jobExecution.jobId}") Object jobId) {
        Long cmJobId = (Long) jobId;
        return new CMProcessor(cmJobId);
    }


    @Bean
    @StepScope
    public ItemWriter<EntityTrk> cmItemWriter() {
        CMWriter cmWriter = new CMWriter();
        cmWriter.setEntityTrkRepository(entityTrkRepository);
        return cmWriter;
    }

    @Bean
    public Step slaveCMStep() {
        return steps.get(slaveStepName)
                .<DataInput, EntityTrk>chunk(BATCH_SIZE)
                .reader(cmReader(null, null))
                .processor(cmItemProcessor(null))
                .writer(cmItemWriter())
                .build();
    }

    @Bean
    public Step masterCMStep() {
        return steps.get(masterStepName)
                .partitioner(slaveCMStep().getName(), cmPartitionerAndJobListener())
                .step(slaveCMStep())
                .gridSize(NUMBER_OF_WINDOWS)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Job job() {
        return jobs.get(jobName)
                .listener(cmPartitionerAndJobListener())
                .start(masterCMStep())
                .build();
    }


}
