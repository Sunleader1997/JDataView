package org.sunyaxing.imagine.jdvagent.sender.base;

import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdvagent.sender.JDataViewEventSender;

import java.io.Closeable;

public interface Sender extends Closeable {
    // 全局只能有一个 SENDER
    public static Sender INSTANCE = new JDataViewEventSender();

    public void send(ThreadSpace message);
}
