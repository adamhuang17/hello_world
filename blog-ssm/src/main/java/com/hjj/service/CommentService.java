package com.hjj.service;

import com.hjj.domain.Comment;

import java.util.List;

public interface CommentService {
	List<Comment> selectCommentListByContentId(int id);

	String selectCommentAuthorById(Integer coid);

	int insert(Comment comment);

	List<Comment> selectAllComment();

	int deleteByPrimaryKey(int coid);

	int deleteSelectComment(String[] coids);

	List<Comment> selectCommentListWithUserId(int userId);
}
