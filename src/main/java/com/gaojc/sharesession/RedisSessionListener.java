package com.gaojc.sharesession;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * redisSession事件监听器
 * author:gaojccn
 * email:jcgaogs@163.com
 */
@Service
public class RedisSessionListener implements SessionListener {
    private static final Logger logger = LoggerFactory.getLogger(RedisSessionListener.class);

    @Autowired
    private RedisSessionDao sessionDao;

    @Override
    public void onStart(Session session) {//会话创建时触发
        logger.debug("会话创建：" + session.getId());
    }

    @Override
    public void onExpiration(Session session) {//会话过期时触发
        logger.debug("会话过期：" + session.getId());
        sessionDao.delete(session);
    }

    @Override
    public void onStop(Session session) {//退出时触发
        logger.info("会话停止：" + session.getId());
        sessionDao.delete(session);
    }
}
