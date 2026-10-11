package com.abctelecom.nms.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/** Verifies the AuthFilter redirect rule without a real container. */
class AuthFilterTest {

    @Test
    void anonymousRequestToProtectedPageIsRedirectedToLogin() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getContextPath()).thenReturn("/qitrom");
        when(req.getRequestURI()).thenReturn("/qitrom/dashboard");
        when(req.getSession(false)).thenReturn(null);

        new AuthFilter().doFilter(req, resp, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(resp).sendRedirect("/qitrom/login");
    }

    @Test
    void loginPageIsAlwaysPublic() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getContextPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/login");
        when(req.getSession(false)).thenReturn(null);

        new AuthFilter().doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    void signedInSessionPassesThrough() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getContextPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/dashboard");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Object());

        new AuthFilter().doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
    }
}
