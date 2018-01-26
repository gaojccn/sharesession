package com.gaojc.sharesession;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * RedisSessionDao
 * author:gaojccn
 * email:jcgaogs@163.com
 */
@Service("sessionDao")
public class RedisSessionDao extends AbstractSessionDAO {
    private static Logger logger = LoggerFactory.getLogger(RedisSessionDao.class);

    @Autowired
    private RedisManager redisManager;

    //设置过期时间
    @Value("${session.expireTime}")
    private long expireTime;

    // The Redis key prefix for the sessions
    @Value("${session.keyPrefix}")
    private String keyPrefix;

    @Override
    public Serializable doCreate(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, formatSessionId(sessionId));
        logger.info("session id is" + session.getId());
        this.saveSession(session);
        return session.getId();
    }

    private String formatSessionId(Serializable sid) {
        try {
            String sessionId = String.valueOf(sid).replace("-", "").toUpperCase();
            return sessionId;
        } catch (Exception e) {
            logger.error("formatSessionId happen exception {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            logger.error("session id is null");
            return null;
        }
        Session s = redisManager.get(keyPrefix + sessionId);
        return s;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        this.saveSession(session);
    }

    private void saveSession(Session session) {
        if (session == null || session.getId() == null) {
            logger.error("session or session id is null");
            return;
        }
        session.setTimeout(expireTime);
        redisManager.setEx(keyPrefix + session.getId(), SerializableUtils.serialize(session), expireTime);
    }

    @Override
    public void delete(Session session) {
        if (session == null || session.getId() == null) {
            logger.error("session or session id is null");
            return;
        }
        redisManager.delete(keyPrefix + session.getId());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<Session> sessions = redisManager.keys(this.keyPrefix + "*");
        return sessions;
    }

}
