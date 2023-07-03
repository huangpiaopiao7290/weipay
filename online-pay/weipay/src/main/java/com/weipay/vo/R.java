package com.weipay.vo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 定义前后端数交互的统一格式
 */

public class R {

    private Integer code;                                       // 响应码
    private String message;                                     // 响应消息
    private Map<String, Object> data = new HashMap<>();         // 具体返回数据

    public R() {
    }

    public R(Integer code, String message, Map<String, Object> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public R setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public R setMessage(String message) {
        this.message = message;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public R setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        R r = (R) o;
        return Objects.equals(code, r.code) && Objects.equals(message, r.message) && Objects.equals(data, r.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
    }

    @Override
    public String toString() {
        return "R{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     *  成功的响应
     * @return R 统一的响应格式
     */
    public static R ok() {

        R r = new R();
        r.setCode(2000);                // 成功的响应码2000
        r.setMessage("success");        // 成功的响应消息

        return r;
    }

    /**
     * 失败的响应
     * @return R
     */
    public static R error() {

        R r = new R();
        r.setCode(5000);                // 成功的响应码5000
        r.setMessage("unsuccessful");        // 成功的响应消息

        return r;
    }

    public R data(String key, Object value) {
        data.put(key, value);
        return this;
    }

}
