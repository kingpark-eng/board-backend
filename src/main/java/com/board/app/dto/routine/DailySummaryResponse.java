package com.board.app.dto.routine;

import java.time.LocalDate;

public class DailySummaryResponse {
	public record DailySummaryDto(LocalDate logDate, long done, long total) {}
}
