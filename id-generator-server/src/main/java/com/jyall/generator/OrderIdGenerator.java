package com.jyall.generator;

/**
 * 10位订单生成器,后期会涨到11位,但是不会超过11位
 * "2045-01-01 00:00:00" 的订单号为66137620480
 * Created by shaojieyue
 * on 2015-01-26 13:22
 */
public class OrderIdGenerator {
    private final long epzepoch = 23300000L;
    private volatile long lastts = -1L;
    //序列号所占位数
    private final int sequenceBits = 12;
    //序列号
    private volatile long sequence = 0L;
    //最大序列号
    private final long maxSequence = -1L ^ (-1L << sequenceBits);



    private static class OrderIdGeneratorHolder{
        //单例变量
        private static final OrderIdGenerator orderIdGenerator = new OrderIdGenerator();
    }

    private OrderIdGenerator(){}

    public synchronized long generateLongId() throws InvalidSystemClockException {
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
            //当前时间戳小于已经记录的最大数,说明时间往回调整
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
     * 合并时间戳和序列号
     * @param timestamp
     * @param sequence
     * @return
     */
    private long combine(long timestamp,long sequence){
        long id = ((timestamp - epzepoch) << (sequenceBits)) | sequence;
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

    public static OrderIdGenerator getInstance(){
        return OrderIdGeneratorHolder.orderIdGenerator;
    }

    public class InvalidSystemClockException extends Exception {
        public InvalidSystemClockException(String message){
            super(message);
        }
    }
}
