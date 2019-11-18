package com.xingyun.bbc.mallpc.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BinaryResult<T,U> {
    private T t;
    private U u;
}
