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

import java.util.ArrayList;
import java.util.List;

/**
 * 动态多数据源切换上下文
 *
 * @author PeKnight
 *
 * Created by PeKnight on 2017/6/23.
 */
public class DynamicDataSourceContext {
	private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

	private static final List<String> DATA_SOURCE_NAMES = new ArrayList<>();
    
	public static List<String> getDataSourceNames() {
		return DATA_SOURCE_NAMES;
	}

	public static void setDataSourceName(String dataSourceName) {
		CONTEXT.set(dataSourceName);
	}

	public static String getDataSourceName() {
		return CONTEXT.get();
	}

	public static void clearDataSourceName() {
		CONTEXT.remove();
	}

	public static boolean containsDataSourceName(String dataSourceName){
		return DATA_SOURCE_NAMES.contains(dataSourceName);
	}
}
