package com.team.notify.taskflow.presentation.pages.blocks

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.team.notify.taskflow.domain.model.Block
import com.team.notify.taskflow.presentation.theme.NotionStyle

@Composable
fun HeadingBlock(
    block: Block.Heading,
    index: Int,
    blocks: List<Block>,
    onChange: (List<Block>) -> Unit,
    canEdit: Boolean
) {
    BasicTextField(
        value = block.text,
        enabled = canEdit,
        onValueChange = { newText ->
            onChange(
                blocks.toMutableList().also {
                    it[index] = block.copy(text = newText)
                }
            )
        },
        textStyle = NotionStyle.TitleText,
        decorationBox = { inner ->
            if (block.text.isEmpty()) {
                Text("Heading", color = Color.Gray)
            }
            inner()
        }
    )
}
