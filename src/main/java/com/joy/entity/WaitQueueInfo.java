package com.joy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Joy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaitQueueInfo implements Serializable {

    private Long rank;

    private Long fromTime;
}
