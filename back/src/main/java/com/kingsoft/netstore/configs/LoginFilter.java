package com.kingsoft.netstore.configs;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.kingsoft.netstore.context.Constants;
import com.kingsoft.netstore.context.ContextHolder;
import com.kingsoft.netstore.data.entity.User;
import org.apache.commons.lang3.StringUtils;


/**
 * 用户登录过滤器
 */
public class LoginFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            if (checkLogin(request, response)) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
            ContextHolder.remove();
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private boolean checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String token = request.getHeader("Access-Token");
        User user = (User) session.getAttribute(Constants.LOGIN_USER);
        if (StringUtils.isNotEmpty(token) && user == null) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=utf-8");
            byte[] msg = ("用户[Access-Token=" + token + "]验证失败！").getBytes("utf-8");
            response.getOutputStream().write(msg);
            return true;
        }
        return false;
    }
}