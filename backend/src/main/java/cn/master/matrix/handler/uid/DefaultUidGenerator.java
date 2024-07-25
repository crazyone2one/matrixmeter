package cn.master.matrix.handler.uid;

import cn.master.matrix.exception.CustomException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.TimeUnit;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Slf4j
public class DefaultUidGenerator implements DisposableBean {
    protected long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1693548784);
    protected BitsAllocator bitsAllocator;
    protected long lastSecond = -1L;
    protected long sequence = 0L;
    protected long workerId;
    protected int timeBits = 28;
    protected int workerBits = 22;
    protected int seqBits = 13;
    protected String epochStr = "2023-09-01";
    @Resource
    protected WorkerIdAssigner workerIdAssigner;
    public void init() {
        // init bitsAllocator
        this.setTimeBits(29);
        this.setWorkerBits(21);
        this.setSeqBits(13);
        this.setEpochStr(TimeUtils.getDataStr(System.currentTimeMillis()));

        // initialize bits allocator
        bitsAllocator = new BitsAllocator(timeBits, workerBits, seqBits);

        // initialize worker id
        workerId = workerIdAssigner.assignWorkerId();
        if (workerId > bitsAllocator.getMaxWorkerId()) {
            throw new RuntimeException("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
        }

        log.info("Initialized bits(1, {}, {}, {}) for workerID:{}", timeBits, workerBits, seqBits, workerId);
    }
    public void setTimeBits(int timeBits) {
        if (timeBits > 0) {
            this.timeBits = timeBits;
        }
    }
    public void setWorkerBits(int workerBits) {
        if (workerBits > 0) {
            this.workerBits = workerBits;
        }
    }

    public void setSeqBits(int seqBits) {
        if (seqBits > 0) {
            this.seqBits = seqBits;
        }
    }

    public void setEpochStr(String epochStr) {
        if (StringUtils.isNotBlank(epochStr)) {
            this.epochStr = epochStr;
            this.epochSeconds = TimeUnit.MILLISECONDS.toSeconds(TimeUtils.parseByDayPattern(epochStr).getTime());
        }
    }
    public long getUID() throws CustomException {
        try {
            return nextId();
        } catch (Exception e) {
            log.error("Generate unique id exception. ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() throws Exception {

    }

    private long getCurrentSecond() {
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentSecond - epochSeconds > bitsAllocator.getMaxDeltaSeconds()) {
            throw new CustomException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }

        return currentSecond;
    }

    protected synchronized long nextId() {
        long currentSecond = getCurrentSecond();

        // Clock moved backwards, refuse to generate uid
        if (currentSecond < lastSecond) {
            long refusedSeconds = lastSecond - currentSecond;
            throw new CustomException("Clock moved backwards. Refusing for %d seconds" + refusedSeconds);
        }

        // At the same second, increase sequence
        if (currentSecond == lastSecond) {
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            // Exceed the max sequence, we wait the next second to generate uid
            if (sequence == 0) {
                currentSecond = getNextSecond(lastSecond);
            }

            // At the different second, sequence restart from zero
        } else {
            sequence = 0L;
        }

        lastSecond = currentSecond;

        // Allocate bits for UID
        return bitsAllocator.allocate(currentSecond - epochSeconds, workerId, sequence);
    }
    private long getNextSecond(long lastTimestamp) {
        long timestamp = getCurrentSecond();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentSecond();
        }

        return timestamp;
    }
}
