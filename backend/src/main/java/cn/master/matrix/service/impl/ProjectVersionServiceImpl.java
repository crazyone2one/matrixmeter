package cn.master.matrix.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.matrix.entity.ProjectVersion;
import cn.master.matrix.mapper.ProjectVersionMapper;
import cn.master.matrix.service.ProjectVersionService;
import org.springframework.stereotype.Service;

/**
 * 版本管理 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:11:29.264113700
 */
@Service
public class ProjectVersionServiceImpl extends ServiceImpl<ProjectVersionMapper, ProjectVersion> implements ProjectVersionService {

}
