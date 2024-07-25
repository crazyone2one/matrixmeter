package cn.master.matrix.handler.converter;

import cn.master.matrix.entity.Template;
import cn.master.matrix.payload.dto.project.ProjectTemplateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Created by 11's papa on 07/19/2024
 **/
@Mapper
public interface ProjectConvertMapper {
    ProjectConvertMapper INSTANCE = Mappers.getMapper(ProjectConvertMapper.class);

    ProjectTemplateDTO templateToProjectTemplateDTO(Template template);
}
