package com.self.generator.core;

import com.self.generator.exception.InvalidSystemClockException;

/**
 * Created by shaojieyue
 * Create at 2016-01-29 15:11
 */
public class ShortIdGenerator extends ShardIdGenerator {

    private final long epzepoch = 23300000L;
    private volatile long lastts = -1L;
    //序列号所占位数
    private final int sequenceBits = 16;
    //序列号
    private volatile long sequence = 0L;
    //最大序列号
    private final long maxSequence = -1L ^ (-1L << sequenceBits);

    //时间戳需要往左移动的bit数=分区bit数+序列号占用bit数
    private final long timestampLeftShift = SHARD_ID_BITS + sequenceBits;
    //分区id需要左移位数
    private final long maxShardIdLeftShift = sequenceBits;
    /**
     * @param shardId 分片id
     */
    public ShortIdGenerator(long shardId) {
        super(shardId);
    }

    @Override
    public long nextId() throws InvalidSystemClockException {
        long timestamp = currentTimeMintue();

        //当前时间并未调整到下一个,则时间戳++
        if (lastts == timestamp) {
            sequence = (sequence + 1) % maxSequence;
            //序列号已经达到最大,等待下一个时刻继续生产id
            if (sequence == 0) {
                //等待下一秒
                timestamp = tilNextSeconds(lastts);
            }
        }else if(lastts > timestamp){
            throw new InvalidSystemClockException("Clock moved backwards.  Refusing to generate id for "+ (
                    lastts - timestamp) +" milliseconds.");
        }else {
            //当前时间已经过渡到下一时刻,那么序列号归0
            sequence = 0;
        }
        //记录当前时间戳
        lastts = timestamp;

        return combine(timestamp,sequence);
    }

    /**
     * 合并timestamp ，shardId，sequence用于生成id
     * @param timestamp 当前时间戳
     * @param sequence 当前循环到的序列号
     * @return
     */
    protected long combine(long timestamp,long sequence){
        long id = ((timestamp - epzepoch)<< timestampLeftShift)//左移，为shardId和sequence腾出bit位
                |(shardId << maxShardIdLeftShift)//左移，为sequence腾出bit位
                |sequence;
        return id;
    }

    /**
     * 循环等待下一秒
     * @param lastts
     * @return
     */
    private long tilNextSeconds(long lastts){
        long timestamp = currentTimeMintue();
        //循环等待,知道进入下一秒
        while (timestamp <= lastts) {
            timestamp = currentTimeMintue();
        }
        return timestamp;
    }

    /**
     * 当前的分钟
     * @return
     */
    private long currentTimeMintue(){
        return System.currentTimeMillis()/60000;
    }

}
