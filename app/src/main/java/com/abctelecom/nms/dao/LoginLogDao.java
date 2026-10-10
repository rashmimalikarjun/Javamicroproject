package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;

/** login_log writer. Never stores passwords — only username + event type. */
public class LoginLogDao extends BaseDao {
    public void log(String username, String event) throws DaoException {
        update("INSERT INTO login_log (username, event, source_ip) VALUES (?,?,?)",
               username, event, "127.0.0.1");
    }
}
