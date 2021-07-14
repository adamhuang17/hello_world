package com.hjj.service;

import com.hjj.domain.File;

import java.util.List;

public interface FileService {

	List<File> selectAllFile();

	List<File> selectFileListWithUid(int userId);

	int insert(File newFile);

	int deleteByPrimaryKey(int fid);

	File selectFileByFid(int fid);

	int deleteSelectFile(String[] fids);
}
