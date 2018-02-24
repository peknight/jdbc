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
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.validation.BindException;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DynamicDataSourceConfig {

	public static final String PRIMARY_PREFIX = "spring.datasource";

	public static final String SECONDARY_PREFIX = "secondary.datasource.";

	public static final String PRIMARY_DATA_SOURCE_NAME = "primaryDataSource";

	private DataSource primaryDataSource;

	private Map<String, DataSource> secondaryDataSources = new HashMap<>();

	@Bean
	public DataSource dataSource(ConfigurableEnvironment environment) throws BindException {
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

	private void initSecondaryDataSources(ConfigurableEnvironment environment, String prefix) throws BindException {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, prefix);
		String dataSourceNames = propertyResolver.getProperty("names");
		if (!StringUtils.isEmpty(dataSourceNames)) {
			for (String secondaryDataSourceName : dataSourceNames.split("\\s*,\\s*")) {
				DataSource secondaryDataSource = buildDataSource(environment, prefix + secondaryDataSourceName);
				secondaryDataSources.put(secondaryDataSourceName, secondaryDataSource);
				DynamicDataSourceContext.getDataSourceNames().add(secondaryDataSourceName);
			}
		}
	}

	private DataSource buildDataSource(ConfigurableEnvironment environment, String prefix) throws BindException {
		DataSourceProperties properties = createDataSourceProperties(environment, prefix);
		return properties.initializeDataSourceBuilder().build();
	}

	public DataSourceProperties createDataSourceProperties(ConfigurableEnvironment environment, String prefix)
			throws BindException {
		DataSourceProperties properties = new DataSourceProperties();
		PropertiesConfigurationFactory<DataSourceProperties> factory = new PropertiesConfigurationFactory<>(properties);
		MutablePropertySources propertySources = environment.getPropertySources();
		factory.setPropertySources(propertySources);
		factory.setTargetName(prefix);
		factory.bindPropertiesToTarget();
		if (StringUtils.isEmpty(properties.getUrl())) {
			String ip = environment.getProperty(prefix + ".ip");
			if (StringUtils.isEmpty(ip)) {
				ip = environment.getProperty(PRIMARY_PREFIX + ".ip");
			}
			String port = environment.getProperty(prefix + ".port");
			if (StringUtils.isEmpty(port)) {
				port = environment.getProperty(PRIMARY_PREFIX + ".port");
			}
			String urlPrefix = environment.getProperty(prefix + ".prefix");
			if (StringUtils.isEmpty(urlPrefix)) {
				urlPrefix = environment.getProperty(PRIMARY_PREFIX + ".prefix");
			}
			String urlSuffix = environment.getProperty(prefix + ".suffix");
			if (StringUtils.isEmpty(urlSuffix)) {
				urlSuffix = environment.getProperty(PRIMARY_PREFIX + ".suffix");
			}
			String url = "jdbc:" + urlPrefix + ip + ":" + port + urlSuffix;
			properties.setUrl(url);
		}
		return properties;
	}
}
