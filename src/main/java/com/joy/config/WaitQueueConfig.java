package com.joy.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Joy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitQueueConfig {
    /**
     * 是否启动时间查询
     */
    private Boolean searchTimeEnabled = true;

    /**
     * 队列裂隙
     */
    private String waitQueueType = "";
}