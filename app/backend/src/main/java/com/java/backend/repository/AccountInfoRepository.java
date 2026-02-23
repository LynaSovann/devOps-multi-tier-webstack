package com.java.backend.repository;

import com.java.backend.model.UserInfo;
import com.java.backend.model.request.UserInfoRequest;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountInfoRepository {


    @Select("""
                INSERT INTO user_infos
                (firstname, lastname, profile_image, bio, user_id)
                VALUES ('N/A', 'N/A', 'N/A', 'N/A', #{user.userId})
            """)
    void createUserInfo(@Param("user") UserInfoRequest userInfoRequest);


    @Select("""
            SELECT * FROM user_infos WHERE user_id = #{userId}
            """)
    @Results(
            id = "appUserMapper",
            value = {
                    @Result(property = "userInfoId", column = "user_info_id"),
                    @Result(property = "profileImage", column = "profile_image"),
                    @Result(property = "userId", column = "user_id"),
            }
    )
    UserInfo getUserInfo(@Param("userId") Integer userId);

    @Update("""
        UPDATE user_infos SET profile_image = #{profileImage}
        WHERE user_id = #{userId}
    """)
    void addProfileImage(@Param("userId") Integer userId, @Param("profileImage") String profileImage);

    @Update("""
        UPDATE user_infos SET profile_image = 'N/A'
        WHERE user_id = #{userId}
    """)
    void deleteProfileImage(Integer userId);
}
