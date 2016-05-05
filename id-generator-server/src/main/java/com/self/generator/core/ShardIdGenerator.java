package com.self.generator.core;

import com.self.generator.exception.IllegalShardIdException;

/**
 * 分片策略id生成器
 * Created by shaojieyue on 11/5/15.
 */
public abstract class ShardIdGenerator implements IdGenerator {
    //分片id所占的bit数
    protected static final long SHARD_ID_BITS = 2L;
    //最大分片id，也就是最大的分布式集群节点数
    protected static final long MAX_SHARD_ID = -1L ^ (-1L << SHARD_ID_BITS);
    //分片id
    protected final long shardId;
    /**
     *
     * @param shardId 分片id
     */
    protected ShardIdGenerator(long shardId){
        this.shardId = shardId;
        if (shardId < 0 || shardId > MAX_SHARD_ID) {
            throw new IllegalShardIdException("illegal shard id ["+shardId+"] shard id must >=0 and <="+ MAX_SHARD_ID);
        }
    }
}
