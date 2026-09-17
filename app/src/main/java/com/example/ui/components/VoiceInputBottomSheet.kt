package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputBottomSheet(
  sheetState: SheetState,
  isListening: Boolean,
  heardText: String,
  parsedExpression: String,
  errorMessage: String?,
  onStartListening: () -> Unit,
  onStopListening: () -> Unit,
  onSpeechResult: (String) -> Unit,
  onError: (String) -> Unit,
  onCalculate: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
  var hasAudioPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasAudioPermission = isGranted
    if (!isGranted) {
      onError(context.getString(R.string.voice_permission_required))
    }
  }

  // Create recognizer listener
  fun startSpeechRecognition() {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      onError(context.getString(R.string.voice_input_not_available))
      return
    }

    try {
      speechRecognizer?.destroy()
      val recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            onStartListening()
          }

          override fun onBeginningOfSpeech() {}

          override fun onRmsChanged(rmsdB: Float) {}

          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            onStopListening()
          }

          override fun onError(error: Int) {
            onStopListening()
            val msg = when (error) {
              SpeechRecognizer.ERROR_NO_MATCH -> context.getString(R.string.voice_input_no_speech)
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> context.getString(R.string.voice_input_no_speech)
              SpeechRecognizer.ERROR_AUDIO, SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                context.getString(R.string.voice_input_mic_error)
              else -> "Speech recognition error ($error)"
            }
            onError(msg)
          }

          override fun onResults(results: Bundle?) {
            onStopListening()
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
              onSpeechResult(matches[0])
            } else {
              onError(context.getString(R.string.voice_input_no_speech))
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
              onSpeechResult(matches[0])
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })
      }

      speechRecognizer = recognizer

      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
      }

      recognizer.startListening(intent)
      onStartListening()
    } catch (e: Exception) {
      onError("Error starting speech recognizer: ${e.localizedMessage}")
      onStopListening()
    }
  }

  // Cleanup recognizer on dispose
  DisposableEffect(Unit) {
    onDispose {
      try {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
      } catch (e: Exception) {
        // Ignored
      }
      speechRecognizer = null
    }
  }

  // Auto-prompt permission or start recognition when opened
  LaunchedEffect(Unit) {
    if (!hasAudioPermission) {
      permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    } else {
      startSpeechRecognition()
    }
  }

  // Pulsing animation for listening state
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (isListening) 1.25f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "mic_scale"
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("voice_input_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(22.dp)
            )
          }

          Column {
            Text(
              text = stringResource(R.string.voice_input_title),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = stringResource(R.string.voice_input_hint),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("btn_close_voice")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.close),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Animated Microphone Center Button
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(110.dp)
      ) {
        if (isListening) {
          Box(
            modifier = Modifier
              .size(100.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
          )
        }

        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
              if (isListening) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable {
              if (isListening) {
                try {
                  speechRecognizer?.stopListening()
                } catch (e: Exception) {}
                onStopListening()
              } else {
                if (!hasAudioPermission) {
                  permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                  startSpeechRecognition()
                }
              }
            }
            .testTag("btn_mic_action"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = "Microphone",
            tint = if (isListening) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Listening Status text
      Text(
        text = if (isListening) stringResource(R.string.voice_input_listening)
        else if (heardText.isNotBlank()) "Tap microphone to speak again"
        else "Tap microphone to dictate",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal,
        color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Error banner
      if (errorMessage != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            textAlign = TextAlign.Center
          )
        }
      }

      // Recognized Text Card
      if (heardText.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("card_heard_text")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = stringResource(R.string.voice_input_recognized),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "\"$heardText\"",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // Interpreted Mathematical Expression Card
      if (parsedExpression.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("card_parsed_expression")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = stringResource(R.string.voice_input_interpreted),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = parsedExpression,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Calculate Now
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              if (hasAudioPermission) {
                startSpeechRecognition()
              } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              }
            },
            modifier = Modifier
              .weight(1f)
              .testTag("btn_voice_try_again")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.voice_input_try_again))
          }

          Button(
            onClick = onCalculate,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("btn_voice_calculate")
          ) {
            Icon(
              imageVector = Icons.Default.Calculate,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.voice_input_calculate),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
