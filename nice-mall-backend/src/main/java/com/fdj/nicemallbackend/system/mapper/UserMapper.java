package com.fdj.nicemallbackend.system.mapper;

import com.fdj.nicemallbackend.system.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xns
 * @since 2019-08-16
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 返回所有用户的姓名
     * @return
     */
    List<String> selectAllName();

    /**
     * 将用户信息插入数据库
     * @param user
     */
    void insertsave(User user);

    /**
     * 通过名称查询用户信息
     * @param username
     * @return
     */
    User findByName(String username);
}