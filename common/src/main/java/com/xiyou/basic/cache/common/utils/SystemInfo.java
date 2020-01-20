package com.xiyou.basic.cache.common.utils;

/**
 * Created by baiyc
 * 2020/1/19/019 17:17
 * Description：系统信息
 */
public class SystemInfo {
    private String requestIp;
    private String userId;

    public SystemInfo() {
    }

    public String getRequestIp() {
        return this.requestIp;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof SystemInfo)) {
            return false;
        } else {
            SystemInfo other = (SystemInfo)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                String this$requestIp = this.getRequestIp();
                String other$requestIp = other.getRequestIp();
                if(this$requestIp == null) {
                    if(other$requestIp != null) {
                        return false;
                    }
                } else if(!this$requestIp.equals(other$requestIp)) {
                    return false;
                }

                String this$userId = this.getUserId();
                String other$userId = other.getUserId();
                if(this$userId == null) {
                    if(other$userId != null) {
                        return false;
                    }
                } else if(!this$userId.equals(other$userId)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof SystemInfo;
    }

    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        String $requestIp = this.getRequestIp();
        int result1 = result * 59 + ($requestIp == null?43:$requestIp.hashCode());
        String $userId = this.getUserId();
        result1 = result1 * 59 + ($userId == null?43:$userId.hashCode());
        return result1;
    }

    public String toString() {
        return "SystemInfo(requestIp=" + this.getRequestIp() + ", userId=" + this.getUserId() + ")";
    }
}
