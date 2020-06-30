package com.androidproject.webservertest;

public class BranchConfig {
    private String code;
    private String name;
    private String check_process;
    private String uuid;

    public BranchConfig(){

    }

    public BranchConfig(String code, String name, String check_process, String uuid) {
        this.code = code;
        this.name = name;
        this.check_process = check_process;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "BranchConfig{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", check_process='" + check_process + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCheck_process() {
        return check_process;
    }

    public void setCheck_process(String check_process) {
        this.check_process = check_process;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
