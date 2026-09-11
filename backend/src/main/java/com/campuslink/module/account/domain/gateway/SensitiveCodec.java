package com.campuslink.module.account.domain.gateway;

/**
 * 出站端口：敏感信息哈希与加解密（技术方案 7.1）。
 * 领域层只依赖本接口，AES-GCM / HMAC 细节由基础设施适配器实现（DIP）。
 */
public interface SensitiveCodec {

    /** HMAC-SHA256 定长哈希，用于邮箱 / 学号的等值查询 */
    String hash(String plain);

    /** AES-256-GCM 加密（随机 IV），用于 email / phone / 学号的静态加密存储 */
    String encrypt(String plain);

    String decrypt(String encoded);
}
