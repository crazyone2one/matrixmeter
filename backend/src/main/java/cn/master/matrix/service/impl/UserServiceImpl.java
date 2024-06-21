package cn.master.matrix.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.matrix.entity.User;
import cn.master.matrix.mapper.UserMapper;
import cn.master.matrix.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-21T10:54:08.016115500
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
