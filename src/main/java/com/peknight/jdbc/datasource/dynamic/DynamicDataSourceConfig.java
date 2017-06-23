/**
 * MIT License
 *
 * Copyright (c) 2017-2027 PeKnight(JKpeknight@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.peknight.jdbc.datasource.dynamic;

import com.peknight.common.string.StringUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DynamicDataSourceConfig {

	public static final String PRIMARY_PREFIX = "spring.datasource.";

	public static final String SECONDARY_PREFIX = "secondary.datasource.";

	public static final String PRIMARY_DATA_SOURCE_NAME = "primaryDataSource";

	private DataSource primaryDataSource;

	private Map<String, DataSource> secondaryDataSources = new HashMap<>();

	@Bean
	public DataSource dataSource(Environment environment) {
		primaryDataSource = buildDataSource(environment, PRIMARY_PREFIX);
		DynamicDataSourceContext.getDataSourceNames().add(PRIMARY_DATA_SOURCE_NAME);

		initSecondaryDataSources(environment, SECONDARY_PREFIX);
		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put(PRIMARY_DATA_SOURCE_NAME, primaryDataSource);
		targetDataSources.putAll(secondaryDataSources);
		DynamicDataSource dataSource = new DynamicDataSource();
		dataSource.setDefaultTargetDataSource(primaryDataSource);
		dataSource.setTargetDataSources(targetDataSources);
		return dataSource;
	}

	private void initSecondaryDataSources(Environment environment, String prefix) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, prefix);
		String dataSourceNames = propertyResolver.getProperty("list");
		if (!StringUtils.isEmpty(dataSourceNames)) {
			for (String secondaryDataSourceName : dataSourceNames.split("\\s*,\\s*")) {
				DataSource secondaryDataSource = buildDataSource(environment, prefix + secondaryDataSourceName + ".");
				secondaryDataSources.put(secondaryDataSourceName, secondaryDataSource);
				DynamicDataSourceContext.getDataSourceNames().add(secondaryDataSourceName);
			}
		}
	}

	private DataSource buildDataSource(Environment environment, String prefix) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, prefix);
		String url = propertyResolver.getProperty("url");
		String username = propertyResolver.getProperty("username");
		String password = propertyResolver.getProperty("password");
		String driverClassName = propertyResolver.getProperty("driver-class-name");
		String typeName = propertyResolver.getProperty("type");
		Class<? extends DataSource> type = null;
		if (!StringUtils.isEmpty(typeName)) {
			try {
				type = (Class<? extends DataSource>) Class.forName(propertyResolver.getProperty("type"));
			} catch (Exception e) {}
		}
		DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create().url(url).username(username).password(password).type(type);
		if (!StringUtils.isEmpty(driverClassName)) {
			dataSourceBuilder.driverClassName(driverClassName);
		}
		return dataSourceBuilder.build();
	}
}
