package com.jyall.generator;

import com.jyall.generator.exception.InvalidSystemClockException;
import java.util.Random;

/**
 * 通用长long id
 * Created by shaojieyue
 * on 2014-12-11 15:31
 */
public class CommonShardIdGenerator extends ShardIdGenerator {
    //时间戳占用的位数
    private final long timestampBits = 41L;
    //序列号占用位数 64-分区占用bit-时间戳占用bit
    private final long sequenceBits = 64L - SHARD_ID_BITS - timestampBits;
    //最大序列号
    private final long maxSequence = -1L ^ (-1L << sequenceBits);
    //时间戳需要往左移动的bit数=分区bit数+序列号占用bit数
    private final long timestampLeftShift = SHARD_ID_BITS + sequenceBits;
    //分区id需要左移位数
    private final long maxShardIdLeftShift = sequenceBits;
    private final long twepoch = 1294537600001L;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;
    /**
     * @param shardId 分片id
     */
    protected CommonShardIdGenerator(long shardId) {
        super(shardId);
    }


    public synchronized long nextId() throws InvalidSystemClockException {
        long timestamp = System.currentTimeMillis();
        if(timestamp<lastTimestamp){
            throw new InvalidSystemClockException("Clock moved backwards.  Refusing to generate id for "+ (
                    lastTimestamp - timestamp) +" milliseconds.");
        }
        if (lastTimestamp == timestamp) {//还没有进入下一毫秒
            sequence = (sequence + 1) & maxSequence;//不明白为何
            if (sequence == 0) {//说明序列号已满，等待到下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;//已经过了最后记录的时间，序列号重新累加
        }
        lastTimestamp = timestamp;
        return combine(timestamp,sequence);
    }

    /**
     * 合并timestamp ，shardId，sequence用于生成id
     * @param timestamp 当前时间戳
     * @param sequence 当前循环到的序列号
     * @return
     */
    protected long combine(long timestamp,long sequence){
        long id = ((timestamp - twepoch)<< timestampLeftShift)//左移，为shardId和sequence腾出bit位
                    |(shardId << maxShardIdLeftShift)//左移，为sequence腾出bit位
                    |sequence;
        return id;
    }

    /**
     * 等待直到下一毫秒
     * @param lastTimestamp 记录的最后时间戳
     * @return
     */
    protected long tilNextMillis(long lastTimestamp){
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {//循环等待，直到当前时间>最后记录的时间戳
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
