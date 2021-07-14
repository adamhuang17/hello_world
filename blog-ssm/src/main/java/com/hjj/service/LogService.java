package com.hjj.service;

import com.hjj.domain.Log;

import java.util.List;

public interface LogService {
	int insert(Log log);

	List<Integer> selectYesterdayPvUvIndexGuestbook();

	List<Integer> selectTodayPvUvIndexGuestbook();

	List<Integer> selectLastWeekPvList();

	List<Integer> selectLastWeekUvList();
}
