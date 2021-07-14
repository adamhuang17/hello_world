package com.hjj.service;

import com.hjj.domain.User;

import java.util.List;

public interface UserService {
	User selectUserByNameAndPassword(User u);

	int insert(User u);

	User findUserByUserName(String userName);

	int updateScreenNameAndMailAndUrl(User u);

	int updatePassword(User u);

	List<User> selectAllUser();

	int deleteByPrimaryKey(int uid);

	int deleteSelectUser(String[] uids);
}
