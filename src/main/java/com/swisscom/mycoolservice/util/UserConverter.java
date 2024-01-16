package com.swisscom.mycoolservice.util;

import java.util.List;
import java.util.stream.Collectors;

import com.swisscom.mycoolservice.beans.User;
import com.swisscom.mycoolservice.entity.UserEntity;

/** responsible to convert user entity to bean and vice versa */
public class UserConverter {

    private UserConverter(){

    }

    public static User convertToUserBean(UserEntity userEntity) {
        return new User(userEntity.getUserName(), userEntity.getEmail());
    }

    public static UserEntity convertToUserEntity(User userBean) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userBean.getUserName());
        userEntity.setEmail(userBean.getEmail());
        return userEntity;
    }

    public static List<User> convertToUserBeans(List<UserEntity> userEntities) {
        return userEntities.stream()
                .map(UserConverter::convertToUserBean)
                .collect(Collectors.toList());
    }
}
