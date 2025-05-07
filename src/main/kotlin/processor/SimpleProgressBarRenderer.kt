package se.gu.processor

import me.tongfei.progressbar.*
import se.gu.ExtendedSyntax.map
import java.time.Duration
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

object SimpleProgressBarRenderer : ProgressBarRenderer {

    private fun formatDuration(duration: Duration): String =
        String.format(
            "%d:%02d:%02d",
            duration.toHours(),
            duration.toMinutesPart(),
            duration.toSecondsPart()
        )

    private fun linearEta(progress: ProgressState): Duration? = when {
        progress.max <= 0 -> null
        progress.isIndefinite -> null
        progress.current - progress.start == 0L -> null

        else -> progress.elapsedAfterStart
            .dividedBy(progress.current - progress.start)
            .multipliedBy(progress.max - progress.current)
    }


    override fun render(progress: ProgressState, maxLength: Int): String {
        val etaString = linearEta(progress).map(SimpleProgressBarRenderer::formatDuration) ?: "?"
        val durationString = formatDuration(progress.totalElapsed)

        val progressBarLength = maxLength - (durationString.length + etaString.length + 13)
        val progressRatio = (progress.current.toDouble() / progress.max.toDouble()).coerceIn(0.0, 1.0)

        val completedChars = ceil(progressRatio * progressBarLength).toInt()
        val incompleteChars = floor((1.0 - progressRatio) * progressBarLength).toInt()

        return String.format(
            "%3d%% [%s%s] (%s / %s)",
            (progressRatio * 100).roundToInt(),
            "=".repeat(completedChars),
            " ".repeat(incompleteChars),
            etaString,
            durationString
        )
    }
}
