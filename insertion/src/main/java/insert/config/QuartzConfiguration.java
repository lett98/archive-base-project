package insert.config;

import common.service.CMService;
import insert.job.InsertQuartzJob;
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

    @Value("${insert.interval}")
    private Integer INTERVAL_IN_SECOND;

    //TODO: rename job
    @Bean
    public JobDetail insertPartitioningJobDetail() {
        //Set Job data map
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", "billInsertingJob");
        jobDataMap.put("jobLauncher", jobLauncher);
        jobDataMap.put("jobLocator", jobLocator);
        jobDataMap.put("cmService", cmService);

        return JobBuilder.newJob(InsertQuartzJob.class)
                .withIdentity("billInsertingJob")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
    }
    @Bean
    public Trigger insertPartitioningJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(INTERVAL_IN_SECOND)
                .repeatForever();

        return TriggerBuilder
                .newTrigger()
                .forJob(insertPartitioningJobDetail())
                .withIdentity("billInsertingJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setTriggers(insertPartitioningJobTrigger());
        scheduler.setQuartzProperties(quartzProperties());
        scheduler.setJobDetails(insertPartitioningJobDetail());
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
