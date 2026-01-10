package com.fdtp.dt;

/**
 * 线程池配置类
 */
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类
 *
 * <p>用于动态线程池的配置管理，支持运行时参数调整和监控</p>
 */
public class FDTPoolConfig {

    // ================ 核心线程池参数 ================

    /**
     * 核心线程数 - 最小工作线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数 - 线程池允许的最大线程数
     */
    private int maximumPoolSize;

    /**
     * 队列容量 - 任务队列大小，-1表示无界队列
     */
    private int queueCapacity;

    /**
     * 线程存活时间 - 非核心线程空闲时的存活时间
     */
    private long keepAliveTime;

    /**
     * 时间单位 - 与keepAliveTime配合使用
     */
    private TimeUnit unit = TimeUnit.MINUTES;

    /**
     * 拒绝策略 - 当任务无法提交时的处理策略
     */
    private String  rejectionPolicy;


    // ================ 动态线程池描述相关参数 ================

    /**
     * 线程池唯一标识 - 用于配置中心识别和监控
     * 默认用uuid生成
     */
    private String poolName= UUID.randomUUID().toString();

    /**
     * 线程池描述 - 业务含义说明
     */
    private String description = "Unnamed ThreadPool";

    /**
     * 业务分组 - 按业务模块分组，用于配置管理和监控聚合
     */
    private String group = "DEFAULT_GROUP";

    /**
     * 环境标识
     */
    private String environment = "prod";


    /**
     * 是否启用动态调整 - false时禁止动态调整
     */
    private boolean dynamicAdjustEnabled = true;


    /**
     * 最后更新时间 - 配置最后修改时间
     */
    private long lastUpdateTime = System.currentTimeMillis();

    /**
     * 配置来源 - 配置中心类型(nacos/apollo/zk)或本地
     */
    private String configSource = "local";

    /**
     * 线程名称前缀 - 用于线程命名
     */
    private String threadNamePrefix="";

    // ================ 监控和告警相关参数 ================
    // todo：　后面补充
    // ================ 监控和告警相关参数 ================


    /**
     * Builder 模式实现
     */
    public static class Builder {

        private FDTPoolConfig  config ;

        /**
         * 默认构造方法
         */
        public Builder() {
            config = new FDTPoolConfig();
        }

        public FDTPoolConfig build(){
            return config;
        }


        // ================ 核心线程池参数构建方法 ================

        /**
         * 设置核心线程数
         *
         * @param corePoolSize 核心线程数
         * @return Builder 实例
         */
        public Builder corePoolSize(int corePoolSize) {
            config.corePoolSize = corePoolSize;
            return this;
        }

        /**
         * 设置最大线程数
         *
         * @param maximumPoolSize 最大线程数
         * @return Builder 实例
         */
        public Builder maximumPoolSize(int maximumPoolSize) {
            config.maximumPoolSize = maximumPoolSize;
            return this;
        }

        /**
         * 设置队列容量
         *
         * @param queueCapacity 队列容量，-1 表示无界队列
         * @return Builder 实例
         */
        public Builder queueCapacity(int queueCapacity) {
            config.queueCapacity = queueCapacity;
            return this;
        }

        /**
         * 设置线程存活时间
         *
         * @param keepAliveTime 线程存活时间
         * @return Builder 实例
         */
        public Builder keepAliveTime(long keepAliveTime) {
            config.keepAliveTime = keepAliveTime;
            return this;
        }

        /**
         * 设置时间单位
         *
         * @param unit 时间单位
         * @return Builder 实例
         */
        public Builder unit(TimeUnit unit) {
            config.unit = unit;
            return this;
        }

        /**
         * 设置拒绝策略
         *
         * @param rejectionPolicy 拒绝策略
         * @return Builder 实例
         */
        public Builder rejectionPolicy(String rejectionPolicy) {
            config.rejectionPolicy = rejectionPolicy;
            return this;
        }

        // ================ 动态线程池描述相关参数构建方法 ================

        /**
         * 设置线程池唯一标识
         *
         * @param poolName 线程池唯一标识
         * @return Builder 实例
         */
        public Builder poolName(String poolName) {
            config.poolName = poolName;
            return this;
        }

        /**
         * 设置线程池描述
         *
         * @param description 线程池描述
         * @return Builder 实例
         */
        public Builder description(String description) {
            config.description = description;
            return this;
        }

        /**
         * 设置业务分组
         *
         * @param group 业务分组
         * @return Builder 实例
         */
        public Builder group(String group) {
            config.group = group;
            return this;
        }

        /**
         * 设置环境标识
         *
         * @param environment 环境标识
         * @return Builder 实例
         */
        public Builder environment(String environment) {
            config.environment = environment;
            return this;
        }

        /**
         * 设置是否启用动态调整
         *
         * @param dynamicAdjustEnabled 是否启用动态调整
         * @return Builder 实例
         */
        public Builder dynamicAdjustEnabled(boolean dynamicAdjustEnabled) {
            config.dynamicAdjustEnabled = dynamicAdjustEnabled;
            return this;
        }

        /**
         * 设置最后更新时间
         *
         * @param lastUpdateTime 最后更新时间
         * @return Builder 实例
         */
        public Builder lastUpdateTime(long lastUpdateTime) {
            config.lastUpdateTime = lastUpdateTime;
            return this;
        }

    }

    @Override
    public String toString() {
        return "PoolConfig{" +
                "corePoolSize=" + corePoolSize +
                ", maximumPoolSize=" + maximumPoolSize +
                ", queueCapacity=" + queueCapacity +
                ", keepAliveTime=" + keepAliveTime +
                ", unit=" + unit +
                ", rejectionPolicy=" + rejectionPolicy +
                ", poolName='" + poolName + '\'' +
                ", description='" + description + '\'' +
                ", group='" + group + '\'' +
                ", environment='" + environment + '\'' +
                ", dynamicAdjustEnabled=" + dynamicAdjustEnabled +
                ", lastUpdateTime=" + lastUpdateTime +
                ", configSource='" + configSource + '\'' +
                ", threadNamePrefix='" + threadNamePrefix + '\'' +
                '}';
    }
}
