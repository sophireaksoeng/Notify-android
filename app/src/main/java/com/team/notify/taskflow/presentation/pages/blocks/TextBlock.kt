package com.team.notify.taskflow.presentation.pages.blocks

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import com.team.notify.taskflow.domain.model.Block
import com.team.notify.taskflow.presentation.theme.NotionStyle

@Composable
fun TextBlock(
    block: Block.Text,
    index: Int,
    blocks: List<Block>,
    onChange: (List<Block>) -> Unit,
    canEdit: Boolean
) {
    BasicTextField(
        value = block.text,
        enabled = canEdit,
        onValueChange = { newText ->
            if (newText.startsWith("/")) return@BasicTextField
            onChange(
                blocks.toMutableList().also {
                    it[index] = block.copy(text = newText)
                }
            )
        },
        textStyle = NotionStyle.BodyText
    )
}
