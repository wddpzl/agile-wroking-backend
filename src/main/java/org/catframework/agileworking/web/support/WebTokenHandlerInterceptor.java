package org.catframework.agileworking.web.support;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.catframework.agileworking.service.WebTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class WebTokenHandlerInterceptor implements HandlerInterceptor {

	@Autowired
	private WebTokenService webTokenService;

	private String[] ignoreUriPatterns;

	private PathMatcher pathMatcher = new AntPathMatcher();
	
	private String tokenKey = "Authorization";

	private String subjectKey = "Subject";

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
		if (isIgnoreUri(req.getRequestURI())) {
			return true;
		} else {
			String token = req.getHeader(tokenKey);
			Assert.notNull(token, "Authorization 不可为空.");
			String subject = req.getHeader(subjectKey);
			Assert.notNull(subject, "Subject 不可为空.");
			Assert.isTrue(webTokenService.verify(subject, token), "Web Token 校验失败.");
			return true;
		}
	}

	private boolean isIgnoreUri(String uri) {
		if (null == ignoreUriPatterns)
			return false;
		return Arrays.asList(ignoreUriPatterns).stream().anyMatch(p -> pathMatcher.match(p, uri));
	}

	/**
	 * TODO 暂时把允许跨域访问的逻辑写在此，实际 PRD 中应该不允许跨域访问.
	 */
	@Override
	public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception e)
			throws Exception {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		resp.setHeader("Access-Control-Max-Age", "3600");
		resp.setHeader("Access-Control-Allow-Headers", "x-requested-with,Authorization");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
	}

	@Override
	public void postHandle(HttpServletRequest req, HttpServletResponse resp, Object handler, ModelAndView m)
			throws Exception {
		// do nothing
	}

	public void setWebTokenService(WebTokenService webTokenService) {
		this.webTokenService = webTokenService;
	}

	@Value("${web.token.ignore.uri.pattern}")
	public void setIgnoreUripattern(String ignoreUripattern) {
		if (!StringUtils.isEmpty(ignoreUripattern))
			ignoreUriPatterns = StringUtils.tokenizeToStringArray(ignoreUripattern, ",");
	}
}