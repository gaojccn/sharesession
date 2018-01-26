package com.gaojc.sharesession;

import org.apache.shiro.session.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

/**
 * RedisSessionDaoTest
 * author:gaojccn
 * email:jcgaogs@163.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring-context.xml")
public class RedisSessionDaoTest {
    @Autowired
    private RedisSessionDao redisSessionDao;

    @Test
    public void doCreate() throws Exception {
//        redisSessionDao.doCreate();
    }

    @Test
    public void doReadSession() throws Exception {
        Session session = redisSessionDao.doReadSession("0B1E7EB22C744C26855FE12DE1DE72B1");
        System.out.println(session);
        System.out.println(session.getHost());
        System.out.println(session.getTimeout());

        Collection<Object> keys = session.getAttributeKeys();

//        session.setAttribute("test", "testValue");
        for (Object key : keys) {
            System.out.println("key=" + key);
            Object attribute = session.getAttribute(key);
            System.out.println("key.value=" + attribute);
        }
    }

    @Test
    public void getActiveSessions() throws Exception {
        Collection<Session> activeSessions = redisSessionDao.getActiveSessions();
        for (Session session : activeSessions) {
            System.out.println(session);
        }
    }

}