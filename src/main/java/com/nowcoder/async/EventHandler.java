package com.nowcoder.async;

import java.util.List;

/**
 * Created by Azog on 2019/5/30.
 */
public interface EventHandler {
    void doHandle(EventModel model);

    List<EventType> getSupportEventTypes();
}
