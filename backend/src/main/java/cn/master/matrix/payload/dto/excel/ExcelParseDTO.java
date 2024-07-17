package cn.master.matrix.payload.dto.excel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Created by 11's papa on 07/17/2024
 **/
@Data
public class ExcelParseDTO<T> {
    private List<T> dataList = new ArrayList<>();
    private TreeMap<Integer, T> errRowData = new TreeMap<>();

    public void addRowData(T t) {
        dataList.add(t);
    }

    public void addErrorRowData(Integer index, T t) {
        errRowData.put(index, t);
    }

    public void addErrorRowDataAll(TreeMap<Integer, T> errRowData) {
        this.errRowData.putAll(errRowData);
    }
}
