package de.crazydev22.discordfs.handlers;

import de.crazydev22.utils.container.Triple;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.TreeSet;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoutingHandler extends IIHandler {
	private final @NotNull RouteSet routes = new RouteSet();
	private final IIHandler EMPTY = new IIHandler(){};

	@NotNull
	private IIHandler getHandler(@NotNull HttpServletRequest request) {
		getLogger().info(request.getRemoteAddr() + " " + request.getMethod() + " " + request.getRequestURL().toString());
		for (var entry : routes) {
			if (request.getRequestURI().matches(entry.getB()) && entry.getC() != null)
				return entry.getC();
		}
		return EMPTY;
	}

	@Override
	public void GET(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request).GET(request, response);
	}

	@Override
	public void HEAD(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request).HEAD(request, response);
	}

	@Override
	public void POST(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request).POST(request, response);
	}

	@Override
	public void PUT(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request).PUT(request, response);
	}

	@Override
	public void DELETE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request).DELETE(request, response);
	}

	@Override
	public void TRACE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request).TRACE(request, response);
	}

	public static class RouteSet extends TreeSet<Triple<@NotNull Integer, @NotNull String, @Nullable IIHandler>> {

		public RouteSet() {
			super(Comparator.comparingInt(Triple::getA));
		}

		public boolean add(int priority, @NotNull String path, @Nullable IIHandler handler) {
			return super.add(new Triple<>(priority, path, handler));
		}
	}
}
