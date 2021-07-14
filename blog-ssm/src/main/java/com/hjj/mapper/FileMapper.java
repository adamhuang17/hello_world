package com.hjj.mapper;

import com.hjj.domain.File;

import java.util.List;

public interface FileMapper {
    int deleteByPrimaryKey(Integer fid);

    int insert(File record);

    int insertSelective(File record);

    File selectByPrimaryKey(Integer fid);

    int updateByPrimaryKeySelective(File record);

    int updateByPrimaryKey(File record);

	List<File> selectAllFile();

	List<File> selectFileListWithUid(int userId);
}