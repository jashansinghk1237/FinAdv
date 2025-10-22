package com.finance.advisor.service;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

@Component
public class StockUpdateScheduler implements SchedulingConfigurer {

    private final StockService stockService;
    private final SchedulingService schedulingService;

    public StockUpdateScheduler(StockService stockService, SchedulingService schedulingService) {
        this.stockService = stockService;
        this.schedulingService = schedulingService;
    }

    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
            () -> stockService.updateStockPrices(),
            triggerContext -> {
                long currentRate = schedulingService.getRateInMilliseconds();
                Instant lastExecution = triggerContext.lastActualExecution();
                Instant nextExecution;
                if (lastExecution == null) {
                    nextExecution = Instant.now().plusMillis(currentRate);
                } else {
                    nextExecution = lastExecution.plusMillis(currentRate);
                }
                return nextExecution;
            }
        );
    }
}

