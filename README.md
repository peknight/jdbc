# Pek JDBC

### 作者

**PeKnight**，Java码农，2015年毕业的新司机。

#### 联系方式

* E-mail peknight@qq.com
* Web-Site [PeKnight.com(尚未开发)](http://www.peknight.com/)

***

### 说明

本工程用于实现JDBC相关功能，目前仅含有动态多数据源一个功能实现。

***

### 内容

#### datasource/dynamic 包

动态多数据源包 **用于SpringBoot工程！** 

##### 原作者信息

* @author 单红宇(365384722)
* @myblog http://blog.csdn.net/catoop/
* @create 2016年1月20日

##### 原文链接

[Spring Boot 动态数据源（多数据源自动切换）](http://blog.csdn.net/catoop/article/details/50575038)

本包内容参照上文做了自己的实现，在切换注解上做了一些灵活性的改动，注册Bean时使用了更简洁的方式。

##### 使用方法

导入包内所有类，在需要使用动态多数据源的启动类上添加@EnableDynamicDataSource注解即可。

配置application.properties中的数据源格式请参照本工程中的application.properties文件

在需要切换数据源的方法上添加@SwitchDataSource注解即可配置多数据源切换

@SwitchDataSource注解有四个属性：

* determineDataSourceClass() DetemineDataSource接口的实现类，默认使用SimpleDeterminDataSource。

这个接口提供方法 String getDataSourceName(JoinPoint joinPoint, String value) 根据切点信息与value值来计算目标数据源名称。
默认实现将value原值返回，如有特殊需求请自行实现该接口，并传入该属性。

* value() 作为上述方法的参数，如果未指定上述属性，value即为要切换的数据源名称。如传入空串使用默认数据源

* keep() 默认情况下被注解的方法执行完毕会切换回默认数据源，如果keep设为true则不切换回默认数据源

* error() 默认情况下如果找不到目标数据源名称则会使用默认数据源，如果error设为true则会抛出异常
