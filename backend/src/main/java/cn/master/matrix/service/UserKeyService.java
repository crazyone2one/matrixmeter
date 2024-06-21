package cn.master.matrix.service;

import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.UserKey;

import java.util.List;
import java.util.Optional;

/**
 * 用户api key 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-21T15:31:28.446710600
 */
public interface  UserKeyService extends IService<UserKey> {

    List<UserKey> listAllByUserId(String userId);

    Optional<UserKey> findByToken(String token);
}
