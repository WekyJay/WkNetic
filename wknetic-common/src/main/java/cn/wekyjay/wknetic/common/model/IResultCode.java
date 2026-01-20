package cn.wekyjay.wknetic.common.model;

/**
 * @Description: 允许 Result 对象接收任何实现了此接口的枚举，方便未来拆分微服务。
 * @Title: 响应码顶层接口
 * @author WekyJay
 * @Github: <a href="https://github.com/WekyJay">https://github.com/WekyJay</a>
 * @Date: 2026/1/20 10:30
 */
public interface IResultCode {
    Integer getCode();
    String getMessage();
}