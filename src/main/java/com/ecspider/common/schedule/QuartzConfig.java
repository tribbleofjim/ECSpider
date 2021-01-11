package com.ecspider.common.schedule;

import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author lyifee
 * on 2021/1/11
 */
@Configuration
@Component
public class QuartzConfig {
    @Autowired
    private JobFactory jobFactory;

    @Autowired
    @Qualifier("dataSource")
    private DataSource primaryDataSource;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        //在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();

        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setDataSource(primaryDataSource);
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);//这样当spring关闭时，会等待所有已经启动的quartz job结束后spring才能完全shutdown。
        schedulerFactory.setOverwriteExistingJobs(false);
        schedulerFactory.setStartupDelay(1);
        return schedulerFactory;
    }

    @Bean(name = "quartzScheduler")
    public Scheduler quartzScheduler() throws IOException {
        return schedulerFactoryBean().getScheduler();
    }

    @Bean
    public QuartzInitializerListener executorListener() {
        return new QuartzInitializerListener();
    }
}
