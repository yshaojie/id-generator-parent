package com.self.generator;

import com.self.generator.common.IdTypes;
import com.self.generator.core.IdGeneratorClient;
import com.self.generator.core.WaitException;

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

    public long nextShortId() throws WaitException{
        return nextId(IdTypes.SHORT_ID);
    }
}
