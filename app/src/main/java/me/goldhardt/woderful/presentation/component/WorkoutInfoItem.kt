package me.goldhardt.woderful.presentation.component


import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun WorkoutInfoItem(
    value: String,
    text: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colors.secondary,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = value,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontSize = 25.sp
        )
        Text(
            textAlign = TextAlign.Center,
            text = text,
            style = MaterialTheme.typography.body2,
        )
    }
}

@Preview
@Composable
fun SummaryFormatPreview() {
    WorkoutInfoItem(value = "359", text = "Calories")
}