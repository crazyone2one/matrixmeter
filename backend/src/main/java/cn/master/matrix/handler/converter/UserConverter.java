package cn.master.matrix.handler.converter;

import cn.master.matrix.entity.User;
import cn.master.matrix.payload.dto.request.user.UserEditRequest;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
public interface UserConverter {
    //UserConverter INSTANCE = Mappers.getMapper( UserConverter.class );
    User convert(UserEditRequest request);
}
