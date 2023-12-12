package de.crazydev22.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class IHandler extends HttpServlet {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	protected void doGet(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws ServletException, IOException {
		try {
			GET(request, response);
		} catch (ServletException | IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	protected void GET(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.doGet(request, response);
	}

	@Override
	protected void doHead(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws ServletException, IOException {
		try {
			HEAD(request, response);
		} catch (ServletException | IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	protected void HEAD(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.doHead(request, response);
	}

	@Override
	protected void doPost(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws ServletException, IOException {
		try {
			POST(request, response);
		} catch (ServletException | IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	protected void POST(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.doPost(request, response);
	}

	@Override
	protected void doPut(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws ServletException, IOException {
		try {
			PUT(request, response);
		} catch (ServletException | IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	protected void PUT(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.doPut(request, response);
	}

	@Override
	protected void doDelete(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws ServletException, IOException {
		try {
			DELETE(request, response);
		} catch (ServletException | IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	protected void DELETE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.doDelete(request, response);
	}

	@Override
	protected void doOptions(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws ServletException, IOException {
		try {
			OPTIONS(request, response);
		} catch (ServletException | IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	protected void OPTIONS(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.doOptions(request, response);
	}

	@Override
	protected void doTrace(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws ServletException, IOException {
		try {
			TRACE(request, response);
		} catch (ServletException | IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	protected void TRACE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.doTrace(request, response);
	}
}
