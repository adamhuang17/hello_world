package com.hjj.service;

import com.hjj.domain.Options;

public interface OptionsService {

	String selectValueByName(String name);

	int updateOptions(Options options);
}
