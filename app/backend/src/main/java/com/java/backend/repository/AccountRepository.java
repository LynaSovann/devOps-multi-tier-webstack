package com.java.backend.repository;

import com.java.backend.model.User;
import com.java.backend.model.request.AccountRequest;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountRepository {

    @Select("""
        SELECT * FROM users WHERE email = #{email}
    """)
    @Results(
            id = "appUserMapper",
            value = {
                    @Result(property = "userId", column = "user_id"),
                    @Result(property = "username", column = "username"),
            }
    )
    User findByEmail(String email);

    @Select("""
        INSERT INTO users 
        ( username, email, password)
        VALUES (#{username}, #{user.email}, #{user.password})
        RETURNING *
    """)
    @ResultMap("appUserMapper")
    User createAccount(@Param("user")AccountRequest accountRequest, String username);

}
