package com.hans.hansbatch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.time.LocalDateTime;

/**
 * @author hans
 */
public class DeliveryDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

        String result = LocalDateTime.now().getHour() < 12 ? "PRESENT" : "NOT PRESENT";
        System.out.println("/n Decider result is " + result + "/n");
        return new FlowExecutionStatus(result);
    }
}
