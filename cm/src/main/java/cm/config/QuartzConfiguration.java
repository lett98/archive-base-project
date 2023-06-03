package cm.config;

import cm.job.CMQuartzJob;
import common.repository.trk.CmTrkRepository;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;

@Configuration
public class QuartzConfiguration {
    @Setter(onMethod = @__(@Autowired))
    private JobLauncher jobLauncher;

    @Setter(onMethod = @__(@Autowired))
    private JobLocator jobLocator;

    @Setter(onMethod = @__(@Autowired))
    private JobRegistry jobRegistry;

    @Setter(onMethod = @__(@Autowired))
    private CmTrkRepository cmTrkRepository;


    @Value("${cm.timewindow}")
    private int SCAN_TIME_RANGE_IN_MINUTES ;
    @Value("${cm.interval}")
    private Integer INTERVAL_IN_SECOND;

    @Bean
    public JobDetail cmPartitioningJobDetail() {
        //Set Job data map
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", "CMInfoJob");
        jobDataMap.put("jobLauncher", jobLauncher);
        jobDataMap.put("jobLocator", jobLocator);
        jobDataMap.put("cmTrkRepository", cmTrkRepository);
        jobDataMap.put("scanTimeRange", SCAN_TIME_RANGE_IN_MINUTES);

        return JobBuilder.newJob(CMQuartzJob.class)
                .withIdentity("CMInfoJob")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
    }
    @Bean
    public Trigger cmPartitioningJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(INTERVAL_IN_SECOND)
                .repeatForever();

        return TriggerBuilder
                .newTrigger()
                .forJob(cmPartitioningJobDetail())
                .withIdentity("CMInfoJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }

    @Bean("cmSchedulerFactory")
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setTriggers(cmPartitioningJobTrigger());
        scheduler.setQuartzProperties(quartzProperties());
        scheduler.setJobDetails(cmPartitioningJobDetail());
        return scheduler;
    }

    @Bean("cmQuartzProperties")
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
}
