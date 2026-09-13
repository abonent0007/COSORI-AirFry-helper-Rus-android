package com.cosory.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosory.app.domain.model.CookingMode

val DeviceButtonBackground = Color(0xFF2B2833)
val DeviceButtonBorder = Color(0xFF6F6A7E)

private object DeviceButtonRegistry {
    val labels: List<String> = CookingMode.entries.map { it.labelEn }

    val regex: Regex = Regex(
        labels.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) },
        RegexOption.IGNORE_CASE,
    )
}

@Composable
fun DeviceButtonChip(
    label: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 2.dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DeviceButtonBackground)
            .border(1.dp, DeviceButtonBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            color = Color.White,
            style = textStyle.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
            maxLines = 1,
        )
    }
}

@Composable
fun DeviceButtonText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val chipTextStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
    )

    val builder = AnnotatedString.Builder()
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    var lastIndex = 0
    var chipNumber = 0

    DeviceButtonRegistry.regex.findAll(text).forEach { match ->
        builder.append(text.substring(lastIndex, match.range.first))
        val label = match.value
        val id = "device_button_${chipNumber++}"
        val labelWidthPx = measurer.measure(AnnotatedString(label.uppercase()), chipTextStyle).size.width
        val chipWidth = with(density) { labelWidthPx.toDp() } + 18.dp
        inlineContent[id] = InlineTextContent(
            placeholder = Placeholder(
                width = with(density) { chipWidth.toSp() },
                height = with(density) { 22.dp.toSp() },
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
            ),
        ) {
            DeviceButtonChip(
                label = label,
                textStyle = chipTextStyle,
                horizontalPadding = 8.dp,
                verticalPadding = 1.dp,
            )
        }
        builder.appendInlineContent(id, label)
        lastIndex = match.range.last + 1
    }
    builder.append(text.substring(lastIndex))

    Text(
        text = builder.toAnnotatedString(),
        inlineContent = inlineContent,
        style = style,
        color = color,
        modifier = modifier,
    )
}
