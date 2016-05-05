package com.self.generator.core;

import com.google.common.io.Closer;
import java.io.Closeable;

/**
 * id生成器server接口类
 * Created by shaojieyue on 11/8/15.
 */
public interface IdGeneratorServer extends Closeable{
    /**
     * 启动服务
     */
    void start();
}
