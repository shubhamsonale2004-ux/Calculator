package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CalculationHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
  sheetState: SheetState,
  historyList: List<CalculationHistory>,
  showClearConfirmDialog: Boolean,
  onDismiss: () -> Unit,
  onItemClick: (CalculationHistory) -> Unit,
  onDeleteItem: (Long) -> Unit,
  onClearAll: () -> Unit,
  onShowClearConfirm: (Boolean) -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("history_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (historyList.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            SuggestionChip(
              onClick = {},
              label = { Text("${historyList.size}") },
              modifier = Modifier.height(28.dp)
            )
          }
        }

        if (historyList.isNotEmpty()) {
          TextButton(
            onClick = { onShowClearConfirm(true) },
            modifier = Modifier.testTag("btn_clear_history")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = stringResource(R.string.clear_history),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = stringResource(R.string.clear_history),
              style = MaterialTheme.typography.labelMedium
            )
          }
        }
      }

      if (historyList.isEmpty()) {
        // Empty State
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.no_history),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = stringResource(R.string.no_history_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      } else {
        // History List
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .heightIn(max = 380.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(historyList, key = { it.id }) { item ->
            HistoryCard(
              item = item,
              onClick = { onItemClick(item) },
              onDelete = { onDeleteItem(item.id) }
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prominent Clear All button at the bottom of the list
        FilledTonalButton(
          onClick = { onShowClearConfirm(true) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_clear_all_history_bottom"),
          colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer
          )
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.clear_history),
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }

  // Clear Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { onShowClearConfirm(false) },
      icon = {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
      },
      title = {
        Text(text = stringResource(R.string.clear_history))
      },
      text = {
        Text(text = stringResource(R.string.clear_history_confirmation))
      },
      confirmButton = {
        TextButton(
          onClick = onClearAll,
          modifier = Modifier.testTag("dialog_confirm_clear")
        ) {
          Text(
            text = stringResource(R.string.confirm_clear),
            color = MaterialTheme.colorScheme.error
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = { onShowClearConfirm(false) },
          modifier = Modifier.testTag("dialog_cancel_clear")
        ) {
          Text(text = stringResource(R.string.cancel))
        }
      }
    )
  }
}

@Composable
fun HistoryCard(
  item: CalculationHistory,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val timeFormatted = formatTimestamp(item.timestamp)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("history_item_${item.id}"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ),
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.expression,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontFamily = FontFamily.Monospace,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "= ${item.result}",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = timeFormatted,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalButton(
          onClick = onClick,
          modifier = Modifier.padding(end = 4.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Restore,
            contentDescription = stringResource(R.string.use_result),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = stringResource(R.string.use_result),
            style = MaterialTheme.typography.labelSmall
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("btn_delete_history_${item.id}")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = stringResource(R.string.delete_entry),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

private fun formatTimestamp(timestamp: Long): String {
  val now = System.currentTimeMillis()
  val diff = now - timestamp
  return when {
    diff < 60_000 -> "Just now"
    diff < 3600_000 -> "${diff / 60_000}m ago"
    diff < 86400_000 -> "${diff / 3600_000}h ago"
    else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
  }
}
