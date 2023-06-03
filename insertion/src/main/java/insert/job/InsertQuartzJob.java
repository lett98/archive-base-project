package insert.job;

import common.model.PickingStatus;
import common.model.TrackingStatus;
import common.model.trk.CmTrk;
import common.service.CMService;
import common.vo.JobEmptyException;
import io.sentry.Sentry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Setter
@Slf4j
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class InsertQuartzJob extends QuartzJobBean {
    private String jobName;
    private JobLauncher jobLauncher;
    private JobLocator jobLocator;
    private CMService cmService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Long jobCMId = checkCondition();
        if (jobCMId == null) {
            log.info(">>> Pending job to perform INSERT is NOT existing.");
            return;
        }
        try {
            Job job = jobLocator.getJob(jobName);
            JobParameters params = new JobParametersBuilder()
                    .addString("Time", String.valueOf(System.currentTimeMillis()))
                    .addLong("JobCMId", jobCMId)
                    .toJobParameters();

            log.info(">>>>> START JOB: " + jobName + " - CmJobId: " + jobCMId);
            jobLauncher.run(job, params);
        } catch (Exception e) {
            Sentry.captureException(e);
            e.printStackTrace();
        }
    }

    protected Long checkCondition(){
        try {
            CmTrk cmTrk = cmService.selectPending(PickingStatus.PENDING, TrackingStatus.PENDING);
            if (cmTrk != null) {
                CmTrk cmTrkPicked = cmService.pick(cmTrk,PickingStatus.PROCESSING, TrackingStatus.PENDING);
                if (cmTrkPicked != null) {
                    Long jobCMId = cmTrkPicked.getJobId();
                    log.info(">>> Chosen CMJob: " + jobCMId);
                    return jobCMId;
                } else return null;
            } else return null;
        } catch (JobEmptyException e) {
            Sentry.captureException(e);
            return null;
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }
    }
}
