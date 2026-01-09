package com.team.notify.taskflow.presentation.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.domain.model.Block
import com.team.notify.taskflow.presentation.pages.blocks.HeadingBlock
import com.team.notify.taskflow.presentation.pages.blocks.TextBlock
import com.team.notify.taskflow.presentation.pages.blocks.TodoBlock

@Composable
fun BlockEditor(
    blocks: List<Block>,
    onChange: (List<Block>) -> Unit,
    canEdit: Boolean
) {
    Column {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is Block.Text -> TextBlock(block, index, blocks, onChange, canEdit)
                is Block.Heading -> HeadingBlock(block, index, blocks, onChange, canEdit)
                is Block.Todo -> TodoBlock(block, index, blocks, onChange, canEdit)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
