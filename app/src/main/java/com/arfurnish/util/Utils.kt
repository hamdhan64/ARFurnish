package com.arfurnish.util

import android.util.Log
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode

object Utils {
    private const val TAG = "ARFurnish_Utils"
    
    val alphabets = mapOf(
    "A" to "bar_chair_round_01_4k.glb",
    "B" to "bar_chair_round_01_4k.glb",
    "C" to "bookshelf.glb",
    "D" to "greenchair_01_4k.glb",
    "E" to "chairs.glb",
    "F" to "mid_century_lounge_chair.glb",
    "G" to "chandelier_01_4k.glb",
    "H" to "chandelier_03_4k.glb",
    "I" to "chinese_armchair_4k.glb",
    "J" to "chinese_cabinet_4k.glb",
    "K" to "chinese_commode_4k.glb",
    "L" to "chinese_screen_panels_4k.glb",
    "M" to "chinese_style.glb",
    "N" to "Closet_and_clothes.glb",
    "O" to "coffee_table_round_01_4k.glb",
    "P" to "doo2.glb",
    "Q" to "door1.glb",
    "R" to "door3.glb",
    "S" to "fan.glb",
    "T" to "gallinera_table_4k.glb",
    "U" to "gothic_coffee_table_4k.glb",
    "V" to "gothiccabinet_01_4k.glb",
    "W" to "greenchair_01_4k.glb",
    "X" to "kitchen_cabinet.glb",
    "Y" to "sofa_01_4k.glb",
    "Z" to "sofa_02_4k.glb",
    "AA" to "mid_century_lounge_chair.glb",
    "BB" to "modern_arm_chair_01_4k.glb",
    "CC" to "modern_coffee_table_01_4k.glb",
    "EE" to "modern_wooden_cabinet_4k.glb",
    "FF" to "ornate_mirror_01_4k.glb",
    "GG" to "ottoman_01_4k.glb",
    "HH" to "outdoor_table_chair_set_01_4k.glb",
    "II" to "park_bench.glb",
    "JJ" to "plaggy.glb",
    "KK" to "plaggy_cc0_inflatable_pool_537.glb",
    "LL" to "plaggy_cc0_shelf-593.glb",
    "MM" to "plaggy_cc0_toilet_roll_holder_616.glb",
    "NN" to "plaggy_cc0_wash_basin_576.glb",
    "OO" to "quaternius_cc0_bathtub_671.glb",
    "PP" to "quaternius_cc0_bunk_bed-682.glb",
    "QQ" to "quaternius_cc0_nightstand_1187.glb",
    "RR" to "quaternius_cc0_red_wood_wall_1458.glb",
    "SS" to "quaternius_cc0_shoji_wall_1461.glb",
    "TT" to "rug.glb",
    "UU" to "rug2.glb",
    "VV" to "skull_chair.glb",
    "WW" to "sofa_01_4k.glb",
    "XX" to "sofa_01_4k.glb",
    "YY" to "sofa_02_4k.glb",
    "ZZ" to "sofa_03_4k.glb",
    "AAA" to "steel_frame_shelves_01_4k.glb",
    "BBB" to "steel_frame_shelves_03_4k.glb",
    "CCC" to "tabel_chair.glb",
    "DDD" to "telephone.glb",
    "EEE" to "trampoline.glb",
    "GGG" to "vintage_wooden_drawer_01_4k.glb",
    "HHH" to "water_cooler.glb",
    "III" to "wet_floor_sign_by_get3dmodels.glb",
    "JJJ" to "whiteboard.glb",
    "KKK" to "wooden_display_shelves_01_4k.glb"
       
    )

    fun getModelForAlphabet(alphabet: String): String {
        val modelName = alphabets[alphabet] ?: error("Model not found for alphabet: $alphabet")
        val modelPath = "models/$modelName"
        Log.d(TAG, "Loading model for alphabet '$alphabet': $modelPath")
        return modelPath
    }

    fun createAnchorNode(
        engine: Engine,
        modelLoader: ModelLoader,
        materialLoader: MaterialLoader,
        modelInstance: MutableList<ModelInstance>,
        anchor: Anchor,
        model: String
    ): AnchorNode {
        Log.d(TAG, "Creating anchor node for model: $model")
        
        val anchorNode = AnchorNode(engine = engine, anchor = anchor)
        
        try {
            val modelNode = ModelNode(
                modelInstance = modelInstance.apply {
                    if (isEmpty()) {
                        Log.d(TAG, "Creating new model instance for: $model")
                        this += modelLoader.createInstancedModel(model, 10)
                    }
                }.removeLast(),
                scaleToUnits = 0.5f  // Increased scale to make models more visible
            ).apply {
                // Disable direct touch editing (pinch/drag/rotate).
                // Transformations are now controlled via on-screen buttons.
                isEditable = false
            }
            
            Log.d(TAG, "Model node created successfully")
            anchorNode.addChildNode(modelNode)
            
            Log.d(TAG, "Anchor node setup completed successfully")
            return anchorNode
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating anchor node for model: $model", e)
            throw e
        }
    }

    fun randomModel(): Pair<String, String> {
        val randomIndex = (0 until alphabets.size).random()
        val alphabet = alphabets.keys.elementAt(randomIndex)
        return Pair(alphabet, getModelForAlphabet(alphabet))
    }
}
