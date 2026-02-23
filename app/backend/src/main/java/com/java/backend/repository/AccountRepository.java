package com.java.backend.repository;

import com.java.backend.model.User;
import com.java.backend.model.request.AccountRequest;
import com.java.backend.model.response.AccountResponse;
import org.apache.ibatis.annotations.*;

import java.util.List;

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
                (username, email, password)
                VALUES ('N/A', #{user.email}, #{user.password})
                RETURNING *
            """)
    @ResultMap("appUserMapper")
    User createAccount(@Param("user") AccountRequest accountRequest);

    @Update("""
                UPDATE users SET username = #{username} WHERE email = #{email}
            """)
    Boolean updateAccount(String username, String email);

    @Select("""
            SELECT * FROM users WHERE user_id = #{id}
            """)
    User findById(@Param("id") Integer id);

    @Select("""
                SELECT * FROM users
            """)
    @ResultMap("appUserMapper")
    List<User> getAllAccounts();
}
