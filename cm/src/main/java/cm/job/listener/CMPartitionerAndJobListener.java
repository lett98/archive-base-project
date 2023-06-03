
package cm.job.listener;

import common.model.*;
import common.model.mapper.JobReadWriteCountRowMapper;
import common.model.trk.CmTrk;
import common.repository.trk.CmTrkRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
@Setter
@Slf4j
public class CMPartitionerAndJobListener extends JobExecutionListenerSupport implements Partitioner {
    @Value("${cm.timewindow}")
    private int SCAN_TIME_RANGE_IN_MINUTES ;
    @Value("${cm.partition.number}")
    private int NUMBER_OF_WINDOWS;

    private List<TimedWindow> timedWindows;
    private CmTrk cmTrk;
    private CmTrkRepository cmTrkRepo;
    private String masterStepName;

    private DataSource jobDataSource;

    private void resetAll() {
        this.cmTrk = null;
        this.timedWindows = null;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        resetAll();
        log.info(">>> BeforeJob: JobID: " + jobExecution.getJobId());
        jobExecution.getExecutionContext().putLong("jobId", jobExecution.getJobId());
        try {
            timedWindows = calculateTimedWindows(NUMBER_OF_WINDOWS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cmTrk = save(jobExecution);
        super.beforeJob(jobExecution);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        if (timedWindows == null || this.timedWindows.size() == 0) {
            throw new RuntimeException("TimedWindow is null.");
        }
        long partitionSize = this.timedWindows.get(0).getEnd().getTime() - this.timedWindows.get(0).getStart().getTime();
        Map<String, ExecutionContext> result = new HashMap<>();
        int partitionNumber = 0;
        Timestamp min = this.timedWindows.get(0).getStart();
        Timestamp start = min;
        Timestamp end = new Timestamp(start.getTime() + partitionSize);
        Timestamp max = this.timedWindows.get(this.timedWindows.size() - 1).getEnd();
        while (start.before(max)) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + partitionNumber, value);

            if(!end.before(max)) {
                end = max;
            }
            value.put("startTime", start);
            value.put("endTime", end);
            start = new Timestamp(start.getTime() + partitionSize);
            end = new Timestamp(end.getTime() + partitionSize);

            partitionNumber++;
        }
        return result;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info(">>> AfterCMJob: JobID: " + jobExecution.getJobId() + " -Status: " + jobExecution.getExitStatus().getExitCode());
        JobReadWriterCount jobReadWriterCount = getReadWriteCount(jobExecution);
        if (cmTrk != null) {
            int effected = cmTrkRepo.updateNewSuccessCmJob(
                    jobReadWriterCount.getReadCount(),
                    jobReadWriterCount.getWriteCount(),
                    jobExecution.getExitStatus().getExitCode(),
                    new Timestamp(jobExecution.getEndTime().getTime()),
                    PickingStatus.PENDING.value(),
                    cmTrk.getJobId());
        }
        resetAll();
        super.afterJob(jobExecution);
    }

    private JobReadWriterCount getReadWriteCount(JobExecution jobExecution) {
        String SQL = "select READ_COUNT, WRITE_COUNT from BATCH_STEP_EXECUTION step  " +
                "inner join  " +
                "    (SELECT max(JOB_EXECUTION_ID) JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION " +
                "    WHERE JOB_INSTANCE_ID = ? ) job on job.JOB_EXECUTION_ID = step.JOB_EXECUTION_ID " +
                "where STEP_NAME = ?";
        JdbcOperations jdbcOperations = new JdbcTemplate(jobDataSource);
        return jdbcOperations.queryForObject(SQL,
                new Object[] {jobExecution.getJobId(),masterStepName},
                new JobReadWriteCountRowMapper());
    }

    private CmTrk save(JobExecution jobExecution) {
        Timestamp startTime = this.timedWindows.get(0).getStart();
        Timestamp endTime = this.timedWindows.get(this.timedWindows.size() - 1).getEnd();
        cmTrkRepo.insertNewCmJob(
                jobExecution.getJobId(),
                startTime,
                endTime,
                jobExecution.getExitStatus().getExitCode(),
                JobType.CHECK_AND_MARK.value(),
                new Timestamp(jobExecution.getCreateTime().getTime()),
                null,
                PickingStatus.PENDING.value(),
                TrackingStatus.GENERATING.value());
        CmTrk cmTrk1 =  cmTrkRepo.findFirstByJobId(jobExecution.getJobId());
        return cmTrk1;
    }

    private List<TimedWindow> calculateTimedWindows(int times) throws Exception {
        int SUB_WINDOW_RANGE = SCAN_TIME_RANGE_IN_MINUTES/NUMBER_OF_WINDOWS;
        Timestamp lastEndAt = cmTrkRepo.findMaxEndAt(JobType.CHECK_AND_MARK.value());
        if (lastEndAt == null) {
            throw new Exception(">>> Must set start time.");
        }
        Timestamp nextTime = new Timestamp(lastEndAt.getTime() + TimeUnit.MINUTES.toMillis(SUB_WINDOW_RANGE));

        List<TimedWindow> tws =  new ArrayList<>(4);
        TimedWindow tw1 = new TimedWindow(lastEndAt, nextTime);
        TimedWindow tw2 = new TimedWindow(tw1.getEnd(), new Timestamp(tw1.getEnd().getTime() + TimeUnit.MINUTES.toMillis(SUB_WINDOW_RANGE)));
        TimedWindow tw3 = new TimedWindow(tw2.getEnd(), new Timestamp(tw2.getEnd().getTime() + TimeUnit.MINUTES.toMillis(SUB_WINDOW_RANGE)));
        TimedWindow tw4 = new TimedWindow(tw3.getEnd(), new Timestamp(tw3.getEnd().getTime() + TimeUnit.MINUTES.toMillis(SUB_WINDOW_RANGE)));

        tws.add(tw1);
        tws.add(tw2);
        tws.add(tw3);
        tws.add(tw4);

        return tws;

    }
}
