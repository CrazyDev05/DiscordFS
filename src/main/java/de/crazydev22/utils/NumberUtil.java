package de.crazydev22.utils;

import java.util.Optional;

@SuppressWarnings("unused")
public class NumberUtil {
	public static Optional<Integer> parseInteger(String s) {
		try {
			return Optional.of(Integer.parseInt(s));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static Optional<Long> parseLong(String s) {
		try {
			return Optional.of(Long.parseLong(s));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static Optional<Double> parseDouble(String s) {
		try {
			return Optional.of(Double.parseDouble(s));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static Optional<Float> parseFloat(String s) {
		try {
			return Optional.of(Float.parseFloat(s));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}