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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

import java.lang.reflect.Method;

/**
 * 动态多数据源切换切面
 *
 * @author PeKnight
 *
 * Created by PeKnight on 2017/6/23.
 */
@Aspect
public class DynamicDataSourceAspect {

	@Around("@within(com.peknight.jdbc.datasource.dynamic.SwitchDataSource) || @annotation(com.peknight.jdbc.datasource.dynamic.SwitchDataSource)")
	public Object determineDataSource(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
		String methodName = method.getName();
		Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());

		SwitchDataSource switchDataSource;
		if (method.isAnnotationPresent(SwitchDataSource.class)) {
			switchDataSource = method.getDeclaredAnnotation(SwitchDataSource.class);
		} else {
			switchDataSource = method.getDeclaringClass().getDeclaredAnnotation(SwitchDataSource.class);
		}

		Class<? extends DetermineDataSource> determineDataSourceClass = switchDataSource.determineDataSourceClass();
		DetermineDataSource determineDataSource = determineDataSourceClass.newInstance();
		String dataSourceName = determineDataSource.getDataSourceName(proceedingJoinPoint, switchDataSource.value());
		boolean keep = switchDataSource.keep();
		boolean error = switchDataSource.error();

		if (!StringUtils.isEmpty(dataSourceName)) {
			if (DynamicDataSourceContext.containsDataSourceName(dataSourceName)) {
				DynamicDataSourceContext.setDataSourceName(dataSourceName);
				logger.info("{}(..) Switch DataSource [{}] ...", methodName, dataSourceName);
			} else {
				if (error) {
					logger.error("{}(..) DataSource [{}] does not exist, Please check your settings!", methodName, dataSourceName);
					throw new DataSourceLookupFailureException(String.format("DataSource [%s] does not exist, Please check your settings!", dataSourceName));
				} else {
					logger.warn("{}(..) DataSource [{}] does not exist, Default dataSource will be used!", methodName, dataSourceName);
				}
			}
		}
        Object object = proceedingJoinPoint.proceed();
        if (!keep) {
			DynamicDataSourceContext.clearDataSourceName();
			logger.info("{}(..) Reset DataSource [{}]", methodName, DynamicDataSourceConfig.PRIMARY_DATA_SOURCE_NAME);
		}
        return object;
	}
}
