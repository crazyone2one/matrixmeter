package cn.master.matrix.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.mapper.UserRoleMapper;
import cn.master.matrix.service.BaseUserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户组 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:19:57.911393700
 */
@Service
public class BaseUserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements BaseUserRoleService {

}
