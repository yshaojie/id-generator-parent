package com.self.generator.core;

import java.io.Closeable;

/**
 * id client接口
 * Created by shaojieyue on 11/15/15.
 */
public interface IdGeneratorClient extends Closeable{

    public String getRemoteAddress();

    /**
     * 生成通用long的长id
     * @return
     */
    public long nextCommonId() throws WaitException;

    /**
     * 生成订单id
     * @return
     */
    public long nextShortId() throws WaitException;

}
