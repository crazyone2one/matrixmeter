package cn.master.matrix.util;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public class SubListUtils {
    public static <T> void dealForSubList(List<T> totalList, int batchSize, Consumer<List<T>> subFunc) {
        if (CollectionUtils.isEmpty(totalList)) {
            return;
        }
        List<T> dealList = new ArrayList<>(totalList);
        while (dealList.size() > batchSize) {
            List<T> subList = dealList.subList(0, batchSize);
            subFunc.accept(subList);
            dealList = dealList.subList(subList.size(), dealList.size());
        }
        if (CollectionUtils.isNotEmpty(dealList)) {
            subFunc.accept(dealList);
        }
    }
}
