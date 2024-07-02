package cn.master.matrix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Slf4j
@Component
public class CreateTestResourceService implements CreateProjectResourceService{
    @Override
    public void createResources(String projectId) {
        log.info("默认增加当前项目[{}]TEST资源", projectId);
    }
}
