/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor;

/**
 * 错误上下文
 *
 * @author Clinton Begin
 */
public class ErrorContext {

    /**
     * 获取换行符，在不同操作系统不一样(LF,...)
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();
    /**
     * 每个线程开一个错误上下文，防止多线程问题
     * <p>
     * ThreadLocal 是本地线程存储，它的作用是为变量在每个线程中创建一个副本，每个线程内部都可以使用该副本，线程之间互不影响。
     * <p>
     * ErrorContext 可以看作是线程内部的单例模式：
     * 使用 ThreadLocal 来管理 ErrorContext：
     * 保证了在多线程环境中，每个线程内部可以共用一份 ErrorContext，但多个线程持有的 ErrorContext 互不影响，保证了异常日志的正确输出。
     */
    private static final ThreadLocal<ErrorContext> LOCAL = ThreadLocal.withInitial(ErrorContext::new);

    /**
     * 充当一个中介，在调用store()方法时将当前的ErrorContext保存下来，在调用recall()方法时将该ErrorContext实例传递给LOCAL。
     */
    private ErrorContext stored;
    /**
     * 存储异常存在于哪个资源文件中
     * <p>
     * 例如：### The error may exist in mapper/AuthorMapper.xml
     */
    private String resource;
    /**
     * 存储异常是在做什么操作时发生的
     * <p>
     * 例如：### The error occurred while setting parameters
     */
    private String activity;
    /**
     * 存储哪个对象操作时发生的异常。
     * <p>
     * 例如：### The error may involve defaultParameterMap
     */
    private String object;
    /**
     * 存储异常的概览信息
     * <p>
     * 例如：### Error querying database. Cause: java.sql.SQLSyntaxErrorException: Unknown column 'id2' in 'field list'
     */
    private String message;
    /**
     * 存储发生异常的SQL语句
     * <p>
     * 例如：### SQL: select id2, name, sex, phone from author where name = ?
     */
    private String sql;
    /**
     * 存储详细的Java异常日志
     * <p>
     * 例如：### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'id2' in 'field list' at
     * org.apache.ibatis.exceptions.ExceptionFactory.wrapException(ExceptionFactory.java:30) at
     * org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(DefaultSqlSession.java:150) at
     * org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(DefaultSqlSession.java:141) at
     * org.apache.ibatis.binding.MapperMethod.executeForMany(MapperMethod.java:139) at org.apache.ibatis.binding.MapperMethod.execute(MapperMethod.java:76)
     */
    private Throwable cause;

    /**
     * 确保单例
     */
    private ErrorContext() {
    }

    /**
     * 工厂方法，得到一个实例
     *
     * @return ErrorContext 实例
     */
    public static ErrorContext instance() {
        return LOCAL.get();
    }

    /**
     * 将当前对象保存起来，同时新建一个ErrorContext
     * <p>
     * 低版本写法
     * <code>
     * public ErrorContext store() {
     * <p>
     * &nbsp;&nbsp;stored = this;
     * <p>
     * &nbsp;&nbsp;LOCAL.set(new ErrorContext());
     * <p>
     * &nbsp;&nbsp;return LOCAL.get();
     * <p>
     * }
     * </code>
     * 这种方法会出现问题：
     * <p>
     * 首先明确：
     * <li>ErrorContext 是一种单例</li>
     * <li>stored 是一个实例变量</li>
     * <p>
     * 那么上述写法就做了如下三件事：<br>
     * 第一步：A@ErrorContext.stored = A@ErrorContext <br>
     * 第二步： 新建ErrorContext对象B@ErrorContext <br>
     * 第三步： B@ErrorContext放入 ThreadLocal <br>
     * <p>
     * 注意：B@ErrorContext.stored是空的，那么下次调用recall方法来恢复的时候无法拿到stored下的对象。
     *
     * @return
     */
    public ErrorContext store() {
        ErrorContext newContext = new ErrorContext();
        newContext.stored = this;
        LOCAL.set(newContext);
        return LOCAL.get();
    }

    /**
     * 回复之前存储的ErrorContext
     *
     * @return
     */
    public ErrorContext recall() {
        if (stored != null) {
            LOCAL.set(stored);
            stored = null;
        }
        return LOCAL.get();
    }

    public ErrorContext resource(String resource) {
        this.resource = resource;
        return this;
    }

    public ErrorContext activity(String activity) {
        this.activity = activity;
        return this;
    }

    public ErrorContext object(String object) {
        this.object = object;
        return this;
    }

    public ErrorContext message(String message) {
        this.message = message;
        return this;
    }

    public ErrorContext sql(String sql) {
        this.sql = sql;
        return this;
    }

    public ErrorContext cause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    /**
     * 用来重置变量位变量赋 null值，方便gc的执行，并清空LOCAL
     *
     * @return
     */
    public ErrorContext reset() {
        resource = null;
        activity = null;
        object = null;
        message = null;
        sql = null;
        cause = null;
        LOCAL.remove();
        return this;
    }

    /**
     * 用来拼接异常信息，最终打印的：
     *
     * @return 拼接的异常信息
     */
    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();

        // message
        if (this.message != null) {
            description.append(LINE_SEPARATOR);
            description.append("### ");
            description.append(this.message);
        }

        // resource
        if (resource != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may exist in ");
            description.append(resource);
        }

        // object
        if (object != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may involve ");
            description.append(object);
        }

        // activity
        if (activity != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error occurred while ");
            description.append(activity);
        }

        // sql
        if (sql != null) {
            description.append(LINE_SEPARATOR);
            description.append("### SQL: ");
            description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
        }

        // cause
        if (cause != null) {
            description.append(LINE_SEPARATOR);
            description.append("### Cause: ");
            description.append(cause.toString());
        }

        return description.toString();
    }

}
