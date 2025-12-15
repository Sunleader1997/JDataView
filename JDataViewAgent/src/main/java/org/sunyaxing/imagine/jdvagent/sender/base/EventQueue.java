package org.sunyaxing.imagine.jdvagent.sender.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * client 将待发送的数据批量发送
 * 批量数据取自于 eventQueue
 */
public class EventQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventQueue.class);

    private final BlockingDeque<String> blockingDeque;

    public EventQueue() {
        // 默认仅缓存 1000 条数据
        this.blockingDeque = new LinkedBlockingDeque<>(1000);
    }

    /**
     * 批量拉取数据
     * @return 批量数据
     */
    public List<String> pull() {
        try {
            List<String> dataList = new ArrayList<>();
            // 阻塞式取一条消息 在队列为空时会阻塞，直到队列中有元素可取
            String item = blockingDeque.take();
            // 一次消费多条信息
            dataList.add(item);
            blockingDeque.drainTo(dataList, 999);
            return dataList;
        } catch (Exception e) {
            LOGGER.error("queue 拉取数据异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * [×] put(E element)：将指定元素插入队列，如果队列已满，则阻塞当前线程，直到有空间可用。
     * [×] add(E element)：将指定元素插入队列，如果队列已满，则抛出异常。
     * [√] offer(E element)：将指定元素插入队列，如果队列已满，则返回 false
     */
    public boolean put(String data) {
        // 为了不影响程序正常执行应该选择offer
        return blockingDeque.offer(data);
    }
}
