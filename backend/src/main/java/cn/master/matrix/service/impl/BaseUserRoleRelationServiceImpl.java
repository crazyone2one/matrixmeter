package cn.master.matrix.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.mapper.UserRoleRelationMapper;
import cn.master.matrix.service.BaseUserRoleRelationService;
import org.springframework.stereotype.Service;

/**
 * 用户组关系 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:20:34.166413800
 */
@Service
public class BaseUserRoleRelationServiceImpl extends ServiceImpl<UserRoleRelationMapper, UserRoleRelation> implements BaseUserRoleRelationService {

}
