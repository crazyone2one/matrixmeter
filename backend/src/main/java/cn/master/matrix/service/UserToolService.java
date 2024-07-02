package cn.master.matrix.service;

import cn.master.matrix.entity.User;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.payload.dto.TableBatchProcessDTO;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.master.matrix.entity.table.UserTableDef.USER;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@Service
@RequiredArgsConstructor
public class UserToolService {

    public List<User> selectByIdList(List<String> userIdList) {
        return QueryChain.of(User.class).where(User::getId).in(userIdList).list();
    }

    public List<String> getBatchUserIds(TableBatchProcessDTO request) {
        if (request.isSelectAll()) {
            val userIdList = QueryChain.of(User.class).select(USER.ID).from(USER)
                    .where(USER.ID.eq(request.getCondition().getKeyword())
                            .or(USER.NAME.like(request.getCondition().getKeyword()))
                            .or(USER.EMAIL.like(request.getCondition().getKeyword()))
                            .or(USER.PHONE.like(request.getCondition().getKeyword())))
                    .listAs(String.class);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                userIdList.removeAll(request.getExcludeIds());
            }
            return userIdList;
        } else {
            return request.getSelectIds();
        }
    }
}
