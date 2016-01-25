package com.jyall.generator;

import com.jyall.generator.common.IdTypes;
import com.jyall.generator.core.IdGeneratorClient;
import com.jyall.generator.core.WaitException;

/**
 * 抽象idclient
 * Created by shaojieyue on 11/15/15.
 */
public abstract class AbstarctIdGeneratorClient implements IdGeneratorClient {

    /**
     * 生成long id
     * @param idType 类型id
     * @return long id
     */
    protected abstract long nextId(int idType) throws WaitException;

    public long nextCommonId() throws WaitException{
        return nextId(IdTypes.COMMON_ID);
    }

    public long nextOrderId() throws WaitException{
        return nextId(IdTypes.ORDER_ID);
    }
}
