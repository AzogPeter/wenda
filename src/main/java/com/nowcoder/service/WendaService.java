package com.nowcoder.service;

import org.springframework.stereotype.Service;

/**
 * Created by Azog on 2019/5/30.
 */
@Service
public class WendaService {
    public String getMessage(int userId) {
        return "Hello Message:" + String.valueOf(userId);
    }
}
