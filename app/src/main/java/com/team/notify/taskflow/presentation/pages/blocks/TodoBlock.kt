package com.team.notify.taskflow.presentation.pages.blocks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.domain.model.Block
import com.team.notify.taskflow.presentation.theme.NotionStyle

@Composable
fun TodoBlock(
    block: Block.Todo,
    index: Int,
    blocks: List<Block>,
    onChange: (List<Block>) -> Unit,
    canEdit: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = block.checked,
            onCheckedChange = { checked ->
                onChange(
                    blocks.toMutableList().also {
                        it[index] = block.copy(checked = checked)
                    }
                )
            },
            enabled = canEdit
        )

        Spacer(Modifier.width(8.dp))

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
            textStyle = NotionStyle.BodyText,
            decorationBox = { inner ->
                if (block.text.isEmpty()) {
                    Text("To-do", color = Color.Gray)
                }
                inner()
            },
            modifier = Modifier.weight(1f)
        )
    }
}
