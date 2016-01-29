package com.jyall.generator.core;

import com.jyall.generator.exception.InvalidSystemClockException;

/**
 * id生成器接口
 * Created by shaojieyue on 11/5/15.
 */
public interface IdGenerator {

    /**
     * 生成一个long 类型id
     * @return 返回long 类型的id
     */
    public long nextId() throws InvalidSystemClockException;
}
