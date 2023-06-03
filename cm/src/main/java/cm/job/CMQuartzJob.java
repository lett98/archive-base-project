package cm.job;

import common.repository.trk.CmTrkRepository;
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
public class CMQuartzJob extends QuartzJobBean {
    private String jobName;
    private JobLauncher jobLauncher;
    private JobLocator jobLocator;
    private CmTrkRepository cmTrkRepository;
    private int scanTimeRange;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            boolean isContinuousAccepted = checkCondition();
            if (!isContinuousAccepted) {
                log.info(">>> Quit.");
                return;
            }

            Job job = jobLocator.getJob(jobName);
            JobParameters params = new JobParametersBuilder()
                    .addString("Time", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            log.info(">>>>> START JOB: " + jobName + " - TIME: " + System.currentTimeMillis());
            jobLauncher.run(job, params);
        } catch (Exception e) {
            Sentry.captureException(e);
            e.printStackTrace();
        }
    }

    private boolean checkCondition() {
        return false;
    }
}
