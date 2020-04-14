package com.lxk.libiotcore;

/**
 * @author https://github.com/103style
 * @date 2020/4/14 17:02
 */
public interface HttpCallback<T> {

    void successByValue(T bean);

    void appError(Throwable throwable);

    void finish();
}
