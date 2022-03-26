package com.hans.hansbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class HansBatchApplication {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public JobExecutionDecider decider(){
        return new DeliveryDecider();
    }

    @Bean
    public JobExecutionDecider receiptDecider(){
        return new ReceiptDecider();
    }

    @Bean
    public Step nestedBillingJobStep(){
        return this.stepBuilderFactory.get("nestedBillingJobStep")
                .job(billingJob())
                .build();
    }

    @Bean
    public Step sendInvoiceStep(){
        return this.stepBuilderFactory.get("invoiceStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Invoice is sent to the customer");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Job billingJob(){
        return this.jobBuilderFactory.get("billingJob").start(sendInvoiceStep()).build();
    }

    @Bean
    public Flow deliveryFlow(){
        return new FlowBuilder<SimpleFlow>("deliveryFlow").start(driveToAddressStep())
                //.on("FAILED").to(storePackageStep())
                //.on("FAILED").stop()
                .on("FAILED").fail()
                .from(driveToAddressStep())
                .on("*").to(decider())
                .on("PRESENT").to(givePackageToCustomerStep())
                .next(receiptDecider()).on("CORRECT").to(thankCustomerStep())
                .from(receiptDecider()).on("INCORRECT").to(refundStep())
                .from(decider())
                .on("NOT_PRESENT").to(leaveAtDoorStep())
                .build();
    }

    @Bean
    public StepExecutionListener selectFlowerListener(){
        return new FlowersSelectionStepExecutionListener();
    }

    @Bean
    public Step thankCustomerStep() {
        return this.stepBuilderFactory.get("thankCustomerStep").tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Thanking the customer.");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step refundStep() {
        return this.stepBuilderFactory.get("refundStep").tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Refunding customer money.");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step leaveAtDoorStep() {
        return this.stepBuilderFactory.get("leaveAtDoorStep").tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Leaving package at the door.");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step storePackageStep() {
        return this.stepBuilderFactory.get("storePackageStep").tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Storing the package while the customer address is located.");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }


    @Bean
    public Step givePackageToCustomerStep() {
        return this.stepBuilderFactory.get("givePackageToCustomer").tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Given the package to the customer.");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step driveToAddressStep() {

        boolean GOT_LOST = false;
        return this.stepBuilderFactory.get("driveToAddressStep").tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

                if(GOT_LOST){
                    throw new RuntimeException("Got lost driving to the address");
                }
                System.out.println("Successfully arrived at the address.");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step packageItemStep() {
        return this.stepBuilderFactory.get("packageItemStep").tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
                String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

                System.out.println(String.format("The %s has been packaged on %s.", item, date));
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step selectFlowerStep(){

        return this.stepBuilderFactory.get("selectFlowerStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Gathering flowers for order.\n");
                return RepeatStatus.FINISHED;
            }
        }).listener(selectFlowerListener()).build();

    }

    @Bean
    public Step removeThornsStep(){

        return this.stepBuilderFactory.get("removeThornsStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Remove thorns from roses.\n");
                return RepeatStatus.FINISHED;
            }
        }).build();

    }


    @Bean
    public Step arrangeFlowerStep(){

        return this.stepBuilderFactory.get("arrangeFlowerStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Arranging flowers for order");
                return RepeatStatus.FINISHED;
            }
        }).build();

    }

    @Bean
    public Job prepareFlowers(){
        return this.jobBuilderFactory.get("prepareFLowersJob")
                .start(selectFlowerStep())
                    .on("TRIM_REQUIRED").to(removeThornsStep()).next(arrangeFlowerStep())
                .from(selectFlowerStep())
                    .on("NO_TRIM_REQUIRED").to(arrangeFlowerStep())
                .from(arrangeFlowerStep()).on("*").to(deliveryFlow())
                .end()
                .build();
    }

    @Bean
    public Job deliverPackageJob() {
        return this.jobBuilderFactory.get("deliverPackageJob")
                .start(packageItemStep())
                .on("*").to(deliveryFlow())
                .next(nestedBillingJobStep())
                .end()
                .build();
    }



    public static void main(String[] args) {
        SpringApplication.run(HansBatchApplication.class, args);
    }

}
