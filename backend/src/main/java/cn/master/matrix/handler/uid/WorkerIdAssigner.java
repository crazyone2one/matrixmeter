package cn.master.matrix.handler.uid;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
public interface WorkerIdAssigner {
    /**
     * Assign worker id for {@link DefaultUidGenerator}
     *
     * @return assigned worker id
     */
    long assignWorkerId();
}
