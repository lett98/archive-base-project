package delete.config;

import common.service.CMService;
import delete.job.DeleteQuartzJob;
import lombok.Setter;
import org.quartz.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;

@Configuration
@ComponentScan("common")
public class QuartzConfiguration {
    @Setter(onMethod = @__(@Autowired))
    private JobLauncher jobLauncher;

    @Setter(onMethod = @__(@Autowired))
    private JobLocator jobLocator;

    @Setter(onMethod = @__(@Autowired))
    private JobRegistry jobRegistry;

    @Setter(onMethod = @__(@Autowired))
    private CMService cmService;

    @Value("${delete.daily.batchsize}")
    private Integer DAILY_BATCH_SIZE;

    @Value("${delete.nightly.batchsize}")
    private Integer NIGHTLY_BATCH_SIZE;
    @Value("${delete.daily.interval}")
    private Integer DAILY_INTERVAL_IN_SECOND;

    @Value("${delete.nightly.interval}")
    private Integer NIGHTLY_INTERVAL_IN_SECOND;

    //TODO: rename job
    @Bean
    public JobDetail deletePartitioningJobDetail() {
        //Set Job data map
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", "DeletingJob");
        jobDataMap.put("jobLauncher", jobLauncher);
        jobDataMap.put("jobLocator", jobLocator);
        jobDataMap.put("cmService", cmService);
        jobDataMap.put("dailyBatchSize", DAILY_BATCH_SIZE);
        jobDataMap.put("nightlyBatchSize", NIGHTLY_BATCH_SIZE);

        return JobBuilder.newJob(DeleteQuartzJob.class)
                .withIdentity("DeletingJob")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
    }
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }


    @Bean
    public Trigger dailyJobTrigger() {
        DailyTimeIntervalScheduleBuilder dailyScheduler = DailyTimeIntervalScheduleBuilder
                .dailyTimeIntervalSchedule()
                .startingDailyAt(new TimeOfDay(07,30,00))
                .endingDailyAt(new TimeOfDay(13,00,00))
                .withIntervalInSeconds(DAILY_INTERVAL_IN_SECOND);
        return TriggerBuilder
                .newTrigger()
                .forJob(deletePartitioningJobDetail())
                .withIdentity("DeletingJobTrigger1")
                .withSchedule(dailyScheduler)
                .build();
    }

    @Bean
    public Trigger dailyJobTrigger2() {
        DailyTimeIntervalScheduleBuilder dailyScheduler = DailyTimeIntervalScheduleBuilder
                .dailyTimeIntervalSchedule()
                .startingDailyAt(new TimeOfDay(15,30,00))
                .endingDailyAt(new TimeOfDay(21,59,59))
                .withIntervalInSeconds(DAILY_INTERVAL_IN_SECOND);
        return TriggerBuilder
                .newTrigger()
                .forJob(deletePartitioningJobDetail())
                .withIdentity("DeletingJobTrigger2")
                .withSchedule(dailyScheduler)
                .build();
    }

    @Bean
    public Trigger nightlyJobTrigger1() {
        DailyTimeIntervalScheduleBuilder nightlyScheduler = DailyTimeIntervalScheduleBuilder
                .dailyTimeIntervalSchedule()
                .startingDailyAt(new TimeOfDay(22,00,00))
                .endingDailyAt(new TimeOfDay(23,59,59))
                .withIntervalInSeconds(NIGHTLY_INTERVAL_IN_SECOND);
        return TriggerBuilder
                .newTrigger()
                .forJob(deletePartitioningJobDetail())
                .withIdentity("DeletingJobTrigger3")
                .withSchedule(nightlyScheduler)
                .build();
    }

    @Bean
    public Trigger nightlyJobTrigger2() {
        DailyTimeIntervalScheduleBuilder nightlyScheduler = DailyTimeIntervalScheduleBuilder
                .dailyTimeIntervalSchedule()
                .startingDailyAt(new TimeOfDay(00,00,00))
                .endingDailyAt(new TimeOfDay(07,29,59))
                .withIntervalInSeconds(NIGHTLY_INTERVAL_IN_SECOND);
        return TriggerBuilder
                .newTrigger()
                .forJob(deletePartitioningJobDetail())
                .withIdentity("DeletingJobTrigger4")
                .withSchedule(nightlyScheduler)
                .build();
    }


    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setTriggers(dailyJobTrigger(), dailyJobTrigger2(), nightlyJobTrigger1(), nightlyJobTrigger2());
        scheduler.setQuartzProperties(quartzProperties());
        scheduler.setJobDetails(deletePartitioningJobDetail());
        return scheduler;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
}
