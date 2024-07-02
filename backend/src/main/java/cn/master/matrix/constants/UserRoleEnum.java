package cn.master.matrix.constants;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
public enum UserRoleEnum {
    GLOBAL("global");

    private final String value;

    UserRoleEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
