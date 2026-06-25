package com.arfurnish.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.net.Uri
import android.view.MotionEvent
import android.view.PixelCopy
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.core.content.ContextCompat
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Tune
import com.arfurnish.util.Utils
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

private const val MIN_LOCK_PLANE_EXTENT_M = 0.6f
private const val MIN_LOCK_PLANE_AREA_M2 = 0.6f

private fun isPlaneStableForLock(plane: Plane): Boolean {
    if (plane.trackingState != TrackingState.TRACKING) return false
    if (plane.subsumedBy != null) return false
    val ex = plane.extentX
    val ez = plane.extentZ
    if (ex < MIN_LOCK_PLANE_EXTENT_M || ez < MIN_LOCK_PLANE_EXTENT_M) return false
    if (ex * ez < MIN_LOCK_PLANE_AREA_M2) return false
    return true
}

private fun selectBestLockHit(hits: List<HitResult>): HitResult? {
    return hits.firstOrNull { hit ->
        val plane = hit.trackable as? Plane ?: return@firstOrNull false
        plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
            isPlaneStableForLock(plane) &&
            plane.isPoseInPolygon(hit.hitPose)
    }
}

private data class MultiPlaceItemUi(
    val key: String,
    val name: String,
    val price: String
)

private data class MultiPlacedObjectUi(
    val id: Int,
    val label: String,
    val anchorNode: AnchorNode,
    val modelNode: ModelNode
)

private enum class MultiArTool {
    Move, Vertical, Rotate, Resize
}

private enum class SidePanelTab {
    Placed, Picker
}

@Composable
fun MultiPlaceARScreen(
    navController: NavController,
    dealBundleKeys: String? = null
) {
    val context = LocalContext.current
    // Keep this list aligned with your existing model keys in Utils.alphabets.
    val catalog = remember {
        listOf(
            MultiPlaceItemUi("Y", "Leather Sofa", "Rs. 120,000"),
            MultiPlaceItemUi("O", "Coffee Table", "Rs. 35,000"),
            MultiPlaceItemUi("C", "Bookshelf", "Rs. 56,000"),
            MultiPlaceItemUi("D", "Chair", "Rs. 25,000"),
            MultiPlaceItemUi("E", "Chairs Set", "Rs. 44,000"),
            MultiPlaceItemUi("CCC", "Table Chair Set", "Rs. 40,000"),
            MultiPlaceItemUi("II", "Park Bench", "Rs. 82,000"),
            MultiPlaceItemUi("W", "Green Chair", "Rs. 38,000"),
            MultiPlaceItemUi("PP", "Bunk Bed", "Rs. 180,000"),
            MultiPlaceItemUi("QQ", "Nightstand", "Rs. 40,000"),
            MultiPlaceItemUi("N", "Closet", "Rs. 120,000"),
            MultiPlaceItemUi("BB", "Arm Chair", "Rs. 35,000"),
            MultiPlaceItemUi("EE", "Wooden Cabinet", "Rs. 40,000"),
            MultiPlaceItemUi("JJJ", "Whiteboard", "Rs. 10,000")
        )
    }

    val parsedDealKeys = remember(dealBundleKeys) {
        dealBundleKeys
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { Uri.decode(it).trim() }
            ?.filter { it.isNotEmpty() && catalog.any { c -> c.key == it } }
            ?.distinct()
            .orEmpty()
    }
    val isDealMode = parsedDealKeys.isNotEmpty()
    var selectedKeys by remember(parsedDealKeys) { mutableStateOf(setOf(parsedDealKeys.firstOrNull() ?: "A")) }
    var activeKey by remember(parsedDealKeys) {
        mutableStateOf(parsedDealKeys.firstOrNull() ?: "A")
    }
    var sidePanelTab by remember { mutableStateOf(SidePanelTab.Picker) }
    var panelOpen by remember { mutableStateOf(false) }
    val panelRevealPx = remember { Animatable(0f) }
    val density = LocalDensity.current
    val panelWidth = 320.dp
    val panelWidthPx = with(density) { panelWidth.toPx() }
    val dragScope = rememberCoroutineScope()
    // Reduce AR jank: let gesture handler read latest state without forcing heavy recompositions.
    val activeKeyLatest by rememberUpdatedState(activeKey)

    // SceneView/AR state (keeps your existing placement behavior, but allows multiple placements).
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val cameraNode = rememberARCameraNode(engine = engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view = view)
    val planeRenderer = remember { mutableStateOf(true) }
    // Keep separate instance pools per model so selection switches actually change what gets placed.
    val modelInstancesByPath = remember { mutableMapOf<String, MutableList<ModelInstance>>() }
    val trackingFailureReason = remember { mutableStateOf<TrackingFailureReason?>(null) }
    val frame = remember { mutableStateOf<Frame?>(null) }
    // Manual lock-based surface detection workflow.
    val lockedPlane = remember { mutableStateOf<Plane?>(null) }
    val lockCandidateHit = remember { mutableStateOf<HitResult?>(null) }
    val lockedSurfaceAnchorNode = remember { mutableStateOf<AnchorNode?>(null) }
    val activeModelNode = remember { mutableStateOf<ModelNode?>(null) }
    val selectedObjectId = remember { mutableStateOf<Int?>(null) }
    val placedObjects = remember { mutableStateListOf<MultiPlacedObjectUi>() }
    var nextObjectId by remember { mutableIntStateOf(1) }
    var controlsOpen by remember { mutableStateOf(false) }
    var activeTool by remember { mutableStateOf(MultiArTool.Move) }
    val transformScope = rememberCoroutineScope()
    var pendingCapture by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingCapture) {
            pendingCapture = false
            captureArScreenshotMultiPlace(context = context) { capturing = false }
        } else {
            pendingCapture = false
            capturing = false
            Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Release AR resources when leaving this screen to avoid process kills (signal 9) on back navigation.
    DisposableEffect(Unit) {
        onDispose {
            try {
                planeRenderer.value = false
                childNodes.clear()
                modelInstancesByPath.values.forEach { it.clear() }
                modelInstancesByPath.clear()
                frame.value = null
                lockedPlane.value = null
                lockCandidateHit.value = null
                lockedSurfaceAnchorNode.value = null
                activeModelNode.value = null
                selectedObjectId.value = null
                placedObjects.clear()
            } catch (_: Throwable) {
                // Best-effort cleanup
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            planeRenderer = planeRenderer.value,
            cameraNode = cameraNode,
            materialLoader = materialLoader,
            onTrackingFailureChanged = { trackingFailureReason.value = it },
            onSessionUpdated = { _, updatedFrame ->
                frame.value = updatedFrame
                // Before lock, keep evaluating a "best" plane under the center reticle.
                // After lock, ignore plane refinements/new planes to avoid shifting surfaces.
                if (lockedPlane.value == null) {
                    val w = view.viewport.width.toFloat()
                    val h = view.viewport.height.toFloat()
                    if (w > 0f && h > 0f) {
                        val hits = updatedFrame.hitTest(w / 2f, h / 2f)
                        lockCandidateHit.value = selectBestLockHit(hits)
                    } else {
                        lockCandidateHit.value = null
                    }
                } else {
                    lockCandidateHit.value = null
                }
            },
            sessionConfiguration = { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { e: MotionEvent, node: Node? ->
                    if (node == null) {
                        selectedObjectId.value = null
                        activeModelNode.value = null
                        return@rememberOnGestureListener
                    }

                    val tappedModelNode: ModelNode? = when (node) {
                        is ModelNode -> node
                        else -> node.childNodes.filterIsInstance<ModelNode>().firstOrNull()
                    }

                    val tappedItem = tappedModelNode?.let { mn ->
                        placedObjects.firstOrNull { it.modelNode == mn }
                    }

                    if (tappedItem == null) {
                        selectedObjectId.value = null
                        activeModelNode.value = null
                    } else {
                        selectedObjectId.value = tappedItem.id
                        activeModelNode.value = tappedItem.modelNode
                    }
                }
            )
        )

        // Scan/review overlay: center reticle + lock/unlock.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White.copy(alpha = 0.85f))
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (lockedPlane.value == null) {
                FilledTonalButton(
                    onClick = {
                        val hit = lockCandidateHit.value
                        if (hit == null) {
                            Toast.makeText(context, "Scan a larger flat surface first", Toast.LENGTH_SHORT).show()
                            return@FilledTonalButton
                        }
                        val plane = hit.trackable as? Plane
                        if (plane == null) {
                            Toast.makeText(context, "No plane found to lock", Toast.LENGTH_SHORT).show()
                            return@FilledTonalButton
                        }
                        val anchor = hit.createAnchorOrNull()
                        if (anchor == null) {
                            Toast.makeText(context, "Failed to lock surface", Toast.LENGTH_SHORT).show()
                            return@FilledTonalButton
                        }

                        lockedPlane.value = plane
                        val surfaceNode = AnchorNode(engine = engine, anchor = anchor)
                        lockedSurfaceAnchorNode.value = surfaceNode
                        childNodes += surfaceNode

                        planeRenderer.value = false
                        Toast.makeText(context, "Surface locked", Toast.LENGTH_SHORT).show()
                    },
                    enabled = lockCandidateHit.value != null
                ) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Lock surface")
                }
            } else {
                FilledTonalButton(
                    onClick = {
                        lockedSurfaceAnchorNode.value?.anchor?.detach()
                        lockedSurfaceAnchorNode.value?.let { childNodes.remove(it) }
                        lockedSurfaceAnchorNode.value = null
                        lockedPlane.value = null
                        planeRenderer.value = true
                        selectedObjectId.value = null
                        activeModelNode.value = null
                        Toast.makeText(context, "Surface unlocked", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Filled.LockOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock surface")
                }
            }
        }

        val canAdd = (lockedPlane.value != null) || (lockCandidateHit.value != null)

        fun resolvePlacementHit(currentFrame: Frame): HitResult? {
            return if (lockedPlane.value != null) {
                val w = view.viewport.width.toFloat()
                val h = view.viewport.height.toFloat()
                if (w <= 0f || h <= 0f) null else {
                    val locked = lockedPlane.value
                    currentFrame.hitTest(w / 2f, h / 2f).firstOrNull { hit ->
                        hit.isValid(depthPoint = false, point = false) &&
                            (hit.trackable as? Plane)?.let { hp ->
                                hp == locked && hp.isPoseInPolygon(hit.hitPose)
                            } == true
                    }
                }
            } else {
                lockCandidateHit.value
            }
        }

        fun placeObjectForKey(keyToPlace: String): Boolean {
            val currentFrame = frame.value ?: return false
            val hitToPlace = resolvePlacementHit(currentFrame)
            if (hitToPlace == null) return false
            val anchor = hitToPlace.createAnchorOrNull() ?: return false

            val modelPath = Utils.getModelForAlphabet(keyToPlace)
            val instances = modelInstancesByPath.getOrPut(modelPath) { mutableListOf() }
            val nodeModel = Utils.createAnchorNode(
                engine = engine,
                modelLoader = modelLoader,
                materialLoader = materialLoader,
                modelInstance = instances,
                anchor = anchor,
                model = modelPath
            )
            childNodes += nodeModel

            val modelNode = nodeModel.childNodes.filterIsInstance<ModelNode>().firstOrNull()
            if (modelNode != null) {
                val id = nextObjectId
                val label = catalog.firstOrNull { it.key == keyToPlace }?.name ?: keyToPlace
                placedObjects += MultiPlacedObjectUi(
                    id = id,
                    label = "$label #$id",
                    anchorNode = nodeModel,
                    modelNode = modelNode
                )
                nextObjectId++
                selectedObjectId.value = id
                activeModelNode.value = modelNode
            }
            return true
        }

        fun addSelectedBundle() {
            if (!canAdd) return
            val keyToPlace = activeKeyLatest
            if (keyToPlace.isBlank()) {
                Toast.makeText(context, "Select an item first", Toast.LENGTH_SHORT).show()
                return
            }
            if (!placeObjectForKey(keyToPlace = keyToPlace)) {
                Toast.makeText(context, "No valid surface to place on", Toast.LENGTH_SHORT).show()
            }
        }

        val deleteSelectedObject = fun() {
            val selectedId = selectedObjectId.value ?: return
            val selectedItem = placedObjects.firstOrNull { it.id == selectedId } ?: return
            childNodes.remove(selectedItem.anchorNode)
            placedObjects.remove(selectedItem)
            selectedObjectId.value = null
            activeModelNode.value = null
        }

        // Main action bar: Add, Camera, Filter with consistent spacing.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { addSelectedBundle() },
                    enabled = canAdd,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }

                FilledTonalButton(
                    onClick = {
                        if (capturing) return@FilledTonalButton
                        if (placedObjects.isEmpty()) {
                            Toast.makeText(context, "Place an object first", Toast.LENGTH_SHORT).show()
                            return@FilledTonalButton
                        }
                        capturing = true
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                captureArScreenshotMultiPlace(context = context) { capturing = false }
                            } else {
                                pendingCapture = true
                                writePermissionLauncher.launch(permission)
                            }
                        } else {
                            captureArScreenshotMultiPlace(context = context) { capturing = false }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null)
                }

                Button(
                    onClick = {
                        controlsOpen = !controlsOpen
                        if (!controlsOpen) {
                            panelOpen = false
                            dragScope.launch {
                                panelRevealPx.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 220)
                                )
                            }
                        } else if (!panelOpen) {
                            panelOpen = true
                            dragScope.launch {
                                panelRevealPx.animateTo(
                                    targetValue = panelWidthPx,
                                    animationSpec = tween(durationMillis = 220)
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (controlsOpen) Icons.Filled.Close else Icons.Filled.Tune,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Filter")
                }
            }
        }

        // Grouped manipulation controls (move/rotate/resize/delete)
        AnimatedVisibility(
            visible = controlsOpen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 84.dp)
                .fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                val step = 0.01f
                fun nudge(dx: Float, dy: Float, dz: Float) {
                    val node = activeModelNode.value ?: return
                    transformScope.launch {
                        repeat(5) {
                            val p = node.position
                            node.position = Position(p.x + dx, p.y + dy, p.z + dz)
                            delay(16)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = selectedObjectId.value?.let { id ->
                            val label = placedObjects.firstOrNull { it.id == id }?.label ?: "Object #$id"
                            "Selected: $label"
                        } ?: "Tap an object to select it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { activeTool = MultiArTool.Move },
                            modifier = Modifier.weight(1f),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (activeTool == MultiArTool.Move) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.OpenWith, contentDescription = null)
                        }
                        FilledTonalIconButton(
                            onClick = { activeTool = MultiArTool.Vertical },
                            modifier = Modifier.weight(1f),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (activeTool == MultiArTool.Vertical) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.SwapVert, contentDescription = null)
                        }
                        FilledTonalIconButton(
                            onClick = { activeTool = MultiArTool.Rotate },
                            modifier = Modifier.weight(1f),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (activeTool == MultiArTool.Rotate) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.RotateRight, contentDescription = null)
                        }
                        FilledTonalIconButton(
                            onClick = { activeTool = MultiArTool.Resize },
                            modifier = Modifier.weight(1f),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (activeTool == MultiArTool.Resize) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Straighten, contentDescription = null)
                        }
                    }

                    when (activeTool) {
                        MultiArTool.Move -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(
                                    onClick = { nudge(-step, 0f, 0f) },
                                    enabled = activeModelNode.value != null,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Left") }
                                FilledTonalButton(
                                    onClick = { nudge(step, 0f, 0f) },
                                    enabled = activeModelNode.value != null,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Right") }
                                FilledTonalButton(
                                    onClick = { nudge(0f, 0f, -step) },
                                    enabled = activeModelNode.value != null,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Front") }
                                FilledTonalButton(
                                    onClick = { nudge(0f, 0f, step) },
                                    enabled = activeModelNode.value != null,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Back") }
                            }
                        }

                        MultiArTool.Rotate -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        val node = activeModelNode.value ?: return@FilledTonalButton
                                        transformScope.launch {
                                            repeat(6) {
                                                val current = node.rotation
                                                node.rotation = Rotation(current.x, current.y - 2f, current.z)
                                                delay(16)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = activeModelNode.value != null
                                ) { Text("Left") }
                                FilledTonalButton(
                                    onClick = {
                                        val node = activeModelNode.value ?: return@FilledTonalButton
                                        transformScope.launch {
                                            repeat(6) {
                                                val current = node.rotation
                                                node.rotation = Rotation(current.x, current.y + 2f, current.z)
                                                delay(16)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = activeModelNode.value != null
                                ) { Text("Right") }
                            }
                        }

                        MultiArTool.Resize -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        val node = activeModelNode.value ?: return@FilledTonalButton
                                        transformScope.launch {
                                            repeat(6) {
                                                val current = node.scale
                                                val factor = 1.015f
                                                node.scale = Scale(current.x * factor, current.y * factor, current.z * factor)
                                                delay(16)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = activeModelNode.value != null
                                ) { Text("Increase") }
                                FilledTonalButton(
                                    onClick = {
                                        val node = activeModelNode.value ?: return@FilledTonalButton
                                        transformScope.launch {
                                            repeat(6) {
                                                val current = node.scale
                                                val factor = 0.985f
                                                val nextX = (current.x * factor).coerceAtLeast(0.1f)
                                                val nextY = (current.y * factor).coerceAtLeast(0.1f)
                                                val nextZ = (current.z * factor).coerceAtLeast(0.1f)
                                                node.scale = Scale(nextX, nextY, nextZ)
                                                delay(16)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = activeModelNode.value != null
                                ) { Text("Decrease") }
                            }
                        }

                        MultiArTool.Vertical -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilledTonalButton(
                                    onClick = { nudge(0f, step, 0f) },
                                    enabled = activeModelNode.value != null,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Up") }
                                FilledTonalButton(
                                    onClick = { nudge(0f, -step, 0f) },
                                    enabled = activeModelNode.value != null,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Down") }
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = deleteSelectedObject,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = activeModelNode.value != null
                    ) {
                        Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete selected")
                    }
                }
            }
        }

        val panelState = rememberDraggableState { delta ->
            dragScope.launch {
                val updated = (panelRevealPx.value - delta).coerceIn(0f, panelWidthPx)
                panelRevealPx.snapTo(updated)
            }
        }
        val settlePanel: () -> Unit = {
            val shouldOpen = panelRevealPx.value > panelWidthPx * 0.4f
            panelOpen = shouldOpen
            dragScope.launch {
                panelRevealPx.animateTo(
                    targetValue = if (shouldOpen) panelWidthPx else 0f,
                    animationSpec = tween(durationMillis = 220)
                )
            }
        }

        // Swipe handle (always visible)
        Card(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(width = 24.dp, height = 92.dp)
                .draggable(
                    state = panelState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { settlePanel() }
                ),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        panelOpen = !panelOpen
                        dragScope.launch {
                            panelRevealPx.animateTo(
                                targetValue = if (panelOpen) panelWidthPx else 0f,
                                animationSpec = tween(durationMillis = 220)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (panelRevealPx.value > panelWidthPx * 0.5f) Icons.Filled.KeyboardArrowRight else Icons.Filled.KeyboardArrowLeft,
                    contentDescription = "Toggle panel"
                )
            }
        }

        // Swipeable filter + object list panel.
        if (panelRevealPx.value > 1f) Card(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp, top = 64.dp, bottom = 96.dp)
                .width(panelWidth)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = panelWidthPx - panelRevealPx.value
                }
                .draggable(
                    state = panelState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { settlePanel() }
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Filters & Objects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = if (isDealMode) "Deal bundle mode: full set placement only" else "Pick active model and placed object",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { sidePanelTab = SidePanelTab.Placed },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (sidePanelTab == SidePanelTab.Placed) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) { Text("Placed Objects") }
                    FilledTonalButton(
                        onClick = { sidePanelTab = SidePanelTab.Picker },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (sidePanelTab == SidePanelTab.Picker) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) { Text("Object Picker") }
                }

                if (sidePanelTab == SidePanelTab.Placed) {
                    if (placedObjects.isEmpty()) {
                        Text(
                            text = "No placed objects yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(placedObjects, key = { it.id }) { item ->
                                val selected = item.id == selectedObjectId.value
                                OutlinedButton(
                                    onClick = {
                                        if (selected) {
                                            selectedObjectId.value = null
                                            activeModelNode.value = null
                                        } else {
                                            selectedObjectId.value = item.id
                                            activeModelNode.value = item.modelNode
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
                                        contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                } else {
                    val pickerItems = if (isDealMode) {
                        catalog.filter { it.key in parsedDealKeys }
                    } else {
                        catalog
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pickerItems) { item ->
                            val isActive = item.key == activeKey
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isActive,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                activeKey = item.key
                                                selectedKeys = setOf(item.key)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                        )
                                        Text(
                                            text = item.price,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            activeKey = item.key
                                            selectedKeys = setOf(item.key)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Select")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurfaceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            maxLines = 1
        )
    }
}

private fun captureArScreenshotMultiPlace(
    context: Context,
    onFinished: () -> Unit = {}
) {
    val activity = context as? Activity ?: return
    val decorView = activity.window.decorView
    val width = decorView.width
    val height = decorView.height
    if (width <= 0 || height <= 0) {
        Toast.makeText(context, "AR view not ready", Toast.LENGTH_SHORT).show()
        onFinished()
        return
    }

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    try {
        PixelCopy.request(
            activity.window,
            bitmap,
            { result ->
                if (result != PixelCopy.SUCCESS) {
                    Toast.makeText(context, "Capture failed ($result)", Toast.LENGTH_SHORT).show()
                    onFinished()
                    return@request
                }

                val mainHandler = Handler(Looper.getMainLooper())
                Thread {
                    val uri = saveBitmapToGalleryMultiPlace(context, bitmap)
                    mainHandler.post {
                        if (uri != null) {
                            Toast.makeText(context, "Saved photo to gallery: $uri", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
                        }
                        onFinished()
                    }
                }.start()
            },
            Handler(Looper.getMainLooper())
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Capture error: ${e.message}", Toast.LENGTH_SHORT).show()
        onFinished()
    }
}

private fun saveBitmapToGalleryMultiPlace(context: Context, bitmap: Bitmap): android.net.Uri? {
    val fileName = "AR_${System.currentTimeMillis()}.jpg"
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val nowSeconds = System.currentTimeMillis() / 1000
            val contentValues = ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ARFurnish")
                put(android.provider.MediaStore.Images.Media.DATE_ADDED, nowSeconds)
                put(android.provider.MediaStore.Images.Media.DATE_TAKEN, nowSeconds)
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null

            val writeSucceeded = try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                } != null
            } catch (_: Exception) {
                false
            }
            if (!writeSucceeded) return null

            val finalizeValues = ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
            }
            resolver.update(uri, finalizeValues, null, null)
            resolver.notifyChange(uri, null)
            uri
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val arDir = java.io.File(picturesDir, "ARFurnish").apply { mkdirs() }
            val file = java.io.File(arDir, fileName)
            java.io.FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/jpeg"),
                null
            )
            android.net.Uri.fromFile(file)
        }
    } catch (_: Exception) {
        null
    }
}


