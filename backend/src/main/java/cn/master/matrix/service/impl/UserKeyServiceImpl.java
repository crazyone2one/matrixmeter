package cn.master.matrix.service.impl;

import cn.master.matrix.entity.UserKey;
import cn.master.matrix.mapper.UserKeyMapper;
import cn.master.matrix.service.UserKeyService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户api key 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-21T15:31:28.446710600
 */
@Service
public class UserKeyServiceImpl extends ServiceImpl<UserKeyMapper, UserKey> implements UserKeyService {

    @Override
    public List<UserKey> listAllByUserId(String userId) {
        return queryChain().where(UserKey::getUserId).eq(userId)
                .and(UserKey::getEnable).eq(true)
                .list();
    }

    @Override
    public Optional<UserKey> findByToken(String token) {
        return queryChain().where(UserKey::getAccessKey).eq(token)
                .and(UserKey::getEnable).eq(true)
                .oneOpt();
    }
}
