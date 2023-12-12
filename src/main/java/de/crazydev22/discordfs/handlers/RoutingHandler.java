package de.crazydev22.discordfs.handlers;

import de.crazydev22.utils.container.Triple;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.TreeSet;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoutingHandler extends IIHandler {
	private final @NotNull RouteSet routes = new RouteSet();
	private final IIHandler EMPTY = new IIHandler(){};

	@NotNull
	private IIHandler getHandler(String uri) {
		for (var entry : routes) {
			if (uri.matches(entry.getB()))
				return entry.getC();
		}
		return EMPTY;
	}

	@Override
	public void GET(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request.getRequestURI()).GET(request, response);
	}

	@Override
	public void HEAD(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request.getRequestURI()).HEAD(request, response);
	}

	@Override
	public void POST(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request.getRequestURI()).POST(request, response);
	}

	@Override
	public void PUT(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request.getRequestURI()).PUT(request, response);
	}

	@Override
	public void DELETE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request.getRequestURI()).DELETE(request, response);
	}

	@Override
	public void TRACE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getHandler(request.getRequestURI()).TRACE(request, response);
	}

	public static class RouteSet extends TreeSet<Triple<Integer, String, IIHandler>> {

		public RouteSet() {
			super(Comparator.comparingInt(Triple::getA));
		}

		public boolean add(int priority, String path, IIHandler handler) {
			return super.add(new Triple<>(priority, path, handler));
		}
	}
}
