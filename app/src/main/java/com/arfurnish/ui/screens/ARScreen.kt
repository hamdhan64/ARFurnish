package com.arfurnish.ui.screens

import android.app.Activity
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.view.MotionEvent
import android.view.PixelCopy
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import com.arfurnish.util.Utils
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
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MIN_LOCK_PLANE_EXTENT_M = 0.6f
private const val MIN_LOCK_PLANE_AREA_M2 = 0.6f

private fun isPlaneStableForLock(plane: Plane): Boolean {
    if (plane.trackingState != TrackingState.TRACKING) return false
    // Avoid planes that are being replaced/refined by ARCore.
    if (plane.subsumedBy != null) return false
    val ex = plane.extentX
    val ez = plane.extentZ
    if (ex < MIN_LOCK_PLANE_EXTENT_M || ez < MIN_LOCK_PLANE_EXTENT_M) return false
    if (ex * ez < MIN_LOCK_PLANE_AREA_M2) return false
    return true
}

private fun selectBestLockHit(
    hits: List<HitResult>
): HitResult? {
    return hits.firstOrNull { hit ->
        val plane = hit.trackable as? Plane ?: return@firstOrNull false
        plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
            isPlaneStableForLock(plane) &&
            plane.isPoseInPolygon(hit.hitPose)
    }
}

private data class PlacedObjectUi(
    val id: Int,
    val label: String,
    val anchorNode: AnchorNode,
    val modelNode: ModelNode
)

private enum class ArTool {
    Move, Vertical, Rotate, Resize
}

@Composable
fun ARScreen(navController: NavController, model: String) {
    val context = LocalContext.current
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val cameraNode = rememberARCameraNode(engine = engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view = view)
    val planeRenderer = remember {
        mutableStateOf(true)
    }
    val modelInstance = remember {
        mutableListOf<ModelInstance>()
    }
    val trackingFailureReason = remember {
        mutableStateOf<TrackingFailureReason?>(null)
    }
    val frame = remember {
        mutableStateOf<Frame?>(null)
    }
    val modelPlaced = remember {
        mutableStateOf(false)
    }
    // Manual lock-based surface detection:
    // - Before lock: ARCore continuously detects planes and we show them visually for review.
    // - The user explicitly presses "Lock Surface" to freeze the chosen plane (center reticle).
    // - After lock: we anchor everything only to the locked plane and ignore all other planes/hits.
    val lockedPlane = remember { mutableStateOf<Plane?>(null) }
    val lockCandidateHit = remember { mutableStateOf<HitResult?>(null) }
    val lockedSurfaceAnchorNode = remember { mutableStateOf<AnchorNode?>(null) }
    val activeModelNode = remember { mutableStateOf<ModelNode?>(null) }
    val selectedObjectId = remember { mutableStateOf<Int?>(null) }
    val placedObjects = remember { mutableStateListOf<PlacedObjectUi>() }
    var nextObjectId by remember { mutableIntStateOf(1) }
    var controlsOpen by remember { mutableStateOf(false) }
    var objectPanelOpen by remember { mutableStateOf(false) }
    var activeTool by remember { mutableStateOf(ArTool.Move) }
    val transformScope = rememberCoroutineScope()

    // Prevent crashes on back navigation by releasing heavy AR state when this screen leaves composition.
    DisposableEffect(Unit) {
        onDispose {
            try {
                planeRenderer.value = false
                childNodes.clear()
                modelInstance.clear()
                frame.value = null
                modelPlaced.value = false
                lockedPlane.value = null
                lockCandidateHit.value = null
                lockedSurfaceAnchorNode.value = null
                selectedObjectId.value = null
                placedObjects.clear()
            } catch (_: Throwable) {
                // Best-effort cleanup
            }
        }
    }

    // Runtime permission only for pre-Android 10 (API < 29).
    // For Android 10+ we save via MediaStore scoped insert (no storage permission needed).
    var pendingCapture by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingCapture) {
            pendingCapture = false
            captureArScreenshot(context = context) { capturing = false }
        } else {
            pendingCapture = false
            capturing = false
            Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
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
        onTrackingFailureChanged = {
            trackingFailureReason.value = it
        },
        onSessionUpdated = { _, updatedFrame ->
            frame.value = updatedFrame
            // Before lock, keep evaluating a "best" plane under the center reticle.
            // After lock, intentionally ignore plane updates/refinements to avoid shifting surfaces.
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
                // Tap-to-place is disabled. Taps are used for selection/unselection only.
                if (node == null) {
                    // Empty tap = clear selection (idle).
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

        // Scan/review overlay: center reticle + lock/unlock + add.
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
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Lock surface")
                }
            } else {
                FilledTonalButton(
                    onClick = {
                        // Allow user to unselect/unlock the surface at any time.
                        lockedSurfaceAnchorNode.value?.anchor?.detach()
                        lockedSurfaceAnchorNode.value?.let { childNodes.remove(it) }
                        lockedSurfaceAnchorNode.value = null
                        lockedPlane.value = null
                        planeRenderer.value = true
                        // Also clear any selected object state (neutral state).
                        selectedObjectId.value = null
                        activeModelNode.value = null
                        Toast.makeText(context, "Surface unlocked", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockOpen,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock surface")
                }
            }
        }

        val canAdd = (lockedPlane.value != null) || (lockCandidateHit.value != null)
        val deleteSelectedObject = fun() {
            val selectedId = selectedObjectId.value ?: return
            val selectedItem = placedObjects.firstOrNull { it.id == selectedId } ?: return
            childNodes.remove(selectedItem.anchorNode)
            placedObjects.remove(selectedItem)
            selectedObjectId.value = null
            activeModelNode.value = null
            modelPlaced.value = placedObjects.isNotEmpty()
        }
        val addObject = fun() {
            if (!canAdd) return
            val currentFrame = frame.value ?: return

            // Choose placement hit from center reticle:
            // - If locked: only allow hits on the locked plane.
            // - If not locked: allow the current best candidate plane hit.
            val hitToPlace: HitResult? = if (lockedPlane.value != null) {
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

            if (hitToPlace == null) {
                Toast.makeText(context, "No valid surface to place on", Toast.LENGTH_SHORT).show()
                return
            }

            val anchor = hitToPlace.createAnchorOrNull()
            if (anchor == null) {
                Toast.makeText(context, "Failed to place object", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                val modelPath = Utils.getModelForAlphabet(model)
                val nodeModel = Utils.createAnchorNode(
                    engine = engine,
                    modelLoader = modelLoader,
                    materialLoader = materialLoader,
                    modelInstance = modelInstance,
                    anchor = anchor,
                    model = modelPath
                )
                childNodes += nodeModel
                modelPlaced.value = true

                val modelNode = nodeModel.childNodes
                    .filterIsInstance<ModelNode>()
                    .firstOrNull()
                if (modelNode != null) {
                    val id = nextObjectId
                    placedObjects += PlacedObjectUi(
                        id = id,
                        label = "$model #$id",
                        anchorNode = nodeModel,
                        modelNode = modelNode
                    )
                    nextObjectId++
                    selectedObjectId.value = id
                    activeModelNode.value = modelNode
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Model load error", Toast.LENGTH_SHORT).show()
            }
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
                    onClick = addObject,
                    enabled = canAdd,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
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
                                captureArScreenshot(context = context) { capturing = false }
                            } else {
                                pendingCapture = true
                                writePermissionLauncher.launch(permission)
                            }
                        } else {
                            captureArScreenshot(context = context) { capturing = false }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    
                }
                Button(
                    onClick = {
                        controlsOpen = !controlsOpen
                        if (!controlsOpen) objectPanelOpen = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (controlsOpen) Icons.Filled.Close else Icons.Filled.Tune,
                        contentDescription = "Filter"
                    )
                }
            }
        }

        // Slide-in object selection panel.
        AnimatedVisibility(
            visible = objectPanelOpen,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp, top = 64.dp, bottom = 118.dp)
                .width(220.dp)
                .fillMaxHeight(0.65f),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Objects", style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { objectPanelOpen = false }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
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
                                border = if (selected) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    } else {
                                        Color.Transparent
                                    },
                                    contentColor = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            ) {
                                Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // Collapsible control panel (move/rotate/resize/delete) without blocking full scene.
        AnimatedVisibility(
            visible = controlsOpen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 88.dp)
                .fillMaxWidth(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Controls",
                                style = MaterialTheme.typography.titleMedium
                            )
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
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(onClick = { objectPanelOpen = !objectPanelOpen }) {
                                Text(if (objectPanelOpen) "Hide Objects" else "Show Objects")
                            }
                            IconButton(onClick = { controlsOpen = false }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                            }
                        }
                    }

                    HorizontalDivider()

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
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = { activeTool = ArTool.Move },
                                modifier = Modifier.weight(1f),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (activeTool == ArTool.Move) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    contentColor = if (activeTool == ArTool.Move) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            ) {
                                Icon(imageVector = Icons.Filled.OpenWith, contentDescription = null)
                            }
                            FilledTonalIconButton(
                                onClick = { activeTool = ArTool.Vertical },
                                modifier = Modifier.weight(1f),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (activeTool == ArTool.Vertical) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    contentColor = if (activeTool == ArTool.Vertical) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            ) {
                                Icon(imageVector = Icons.Filled.SwapVert, contentDescription = null)
                            }
                            FilledTonalIconButton(
                                onClick = { activeTool = ArTool.Rotate },
                                modifier = Modifier.weight(1f),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (activeTool == ArTool.Rotate) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    contentColor = if (activeTool == ArTool.Rotate) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.RotateRight, contentDescription = null)
                            }
                            FilledTonalIconButton(
                                onClick = { activeTool = ArTool.Resize },
                                modifier = Modifier.weight(1f),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (activeTool == ArTool.Resize) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    contentColor = if (activeTool == ArTool.Resize) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            ) {
                                Icon(imageVector = Icons.Filled.Straighten, contentDescription = null)
                            }
                        }
                        Text(
                            text = when (activeTool) {
                                ArTool.Move -> "Current tab: Move"
                                ArTool.Vertical -> "Current tab: Vertical"
                                ArTool.Rotate -> "Current tab: Rotate"
                                ArTool.Resize -> "Current tab: Size"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        when (activeTool) {
                            ArTool.Move -> {
                                Text(
                                    text = "Move controls",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilledTonalButton(
                                        onClick = { nudge(-step, 0f, 0f) },
                                        enabled = activeModelNode.value != null,
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) { Text("Left", style = MaterialTheme.typography.labelSmall) }
                                    FilledTonalButton(
                                        onClick = { nudge(step, 0f, 0f) },
                                        enabled = activeModelNode.value != null,
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) { Text("Right", style = MaterialTheme.typography.labelSmall) }
                                    FilledTonalButton(
                                        onClick = { nudge(0f, 0f, -step) },
                                        enabled = activeModelNode.value != null,
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) { Text("Front", style = MaterialTheme.typography.labelSmall) }
                                    FilledTonalButton(
                                        onClick = { nudge(0f, 0f, step) },
                                        enabled = activeModelNode.value != null,
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) { Text("Back", style = MaterialTheme.typography.labelSmall) }
                                }
                            }

                            ArTool.Rotate -> {
                                Text(
                                    text = "Rotate controls",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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

                            ArTool.Resize -> {
                                Text(
                                    text = "Size controls",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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

                            ArTool.Vertical -> {
                                Text(
                                    text = "Vertical controls",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
        }
    }
}

private fun findSurfaceView(view: android.view.View): android.view.SurfaceView? {
    if (view is android.view.SurfaceView) return view
    if (view is android.view.ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            val surfaceView = findSurfaceView(child)
            if (surfaceView != null) return surfaceView
        }
    }
    return null
}

private fun captureArScreenshot(
    context: Context,
    onFinished: () -> Unit = {}
) {
    val activity = context as? Activity ?: return
    val decorView = activity.window.decorView
    val surfaceView = findSurfaceView(decorView)
    
    val width = surfaceView?.width ?: decorView.width
    val height = surfaceView?.height ?: decorView.height
    
    if (width <= 0 || height <= 0) {
        Toast.makeText(context, "AR view not ready", Toast.LENGTH_SHORT).show()
        onFinished()
        return
    }

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    try {
        val callback = PixelCopy.OnPixelCopyFinishedListener { result ->
            if (result != PixelCopy.SUCCESS) {
                Toast.makeText(context, "Capture failed ($result)", Toast.LENGTH_SHORT).show()
                onFinished()
                return@OnPixelCopyFinishedListener
            }

            val mainHandler = Handler(Looper.getMainLooper())
            Thread {
                val uri = saveBitmapToGallery(context, bitmap)
                mainHandler.post {
                    if (uri != null) {
                        Toast.makeText(
                            context,
                            "Saved photo to gallery",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
                    }
                    onFinished()
                }
            }.start()
        }

        if (surfaceView != null) {
            PixelCopy.request(surfaceView, bitmap, callback, Handler(Looper.getMainLooper()))
        } else {
            val activity = context as? Activity ?: return
            PixelCopy.request(activity.window, bitmap, callback, Handler(Looper.getMainLooper()))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Capture error: ${e.message}", Toast.LENGTH_SHORT).show()
        onFinished()
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): android.net.Uri? {
    val fileName = "AR_${System.currentTimeMillis()}.jpg"
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val nowSeconds = System.currentTimeMillis() / 1000
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ARFurnish")
                put(MediaStore.Images.Media.DATE_ADDED, nowSeconds)
                put(MediaStore.Images.Media.DATE_TAKEN, nowSeconds)
                // Mark as pending while we're writing the file.
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
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

            // Finalize the file so it becomes visible in the gallery.
            val finalizeValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
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
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
