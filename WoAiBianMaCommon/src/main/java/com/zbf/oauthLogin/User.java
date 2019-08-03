package com.zbf.oauthLogin;

import com.zbf.common.IdEntity;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 作者：LCG
 * 创建时间：2018/11/25 11:01
 * 描述：
 */
@Data
public class User extends IdEntity {

    private String username;

    private String loginname;

    private String password;

    private String code;

    private String tel;

    private int status;

    private int sex;
    //用户的头像信息
    private String userImage;

    /**
     * 缓存中读取用户数据
     * @param redisTemplate
     * @param userId
     * @return
     */
    public static User getCurrentUser(RedisTemplate redisTemplate,String userId){
        User userinfo = (User)redisTemplate.opsForHash ().get ( userId,"userinfo" );
        return userinfo;
    }

}
